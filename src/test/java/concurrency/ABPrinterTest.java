package concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ABPrinterTest {

    int THREAD_COUNT = 33;

    @Test
    void test3Pairs() throws InterruptedException {
        for(int i = 0; i < 50; i++){ // repeat the test 50 times
            testNPairs(3);
        }
    }

    @Test
    void test10Pairs() throws InterruptedException {
        for(int i = 0; i < 50; i++){ // repeat the test 50 times
            testNPairs(10);
        }
    }

    @Test
    void test87Pairs() throws InterruptedException {
        for (int i = 0; i < 50; i++){ // repeat the test 50 times
            testNPairs(87);
        }
    }

    private void testNPairs(int N) throws InterruptedException {
        testNPairs(N, false);
    }
    private void testNPairs(int N, boolean printLog ) throws InterruptedException {
        final var printer = new ABPrinterSolution(N);

        final CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final StringBuffer out = new StringBuffer();

        for(int i =0; i < THREAD_COUNT; i++){

            if(i % 2 == 0){
                executorService.execute(() -> {
                    try {
                        printer.a(() -> {
                            out.append("A");
                            if (printLog) {
                                System.out.println("Thread " + Thread.currentThread().getName() + " printed A");
                            }
                        });
                    } catch (InterruptedException ignored) { }
                    countDownLatch.countDown();
                });
            } else {
                executorService.execute(() -> {
                    try {
                        printer.b(() -> {
                            out.append("B");
                            if (printLog) {
                                System.out.println("Thread " + Thread.currentThread().getName() + " printed B");
                            }
                        });
                    } catch (InterruptedException ignored) { }
                    countDownLatch.countDown();
                });
            }
        }

        executorService.shutdown();
        countDownLatch.await(); // wait for all threads to finish

        StringBuilder expected = new StringBuilder();
        expected.append("AB".repeat(Math.max(0, N)));
        System.out.println("Actual: " + out);
        System.out.println("Expected: " + expected);
        assertEquals( expected.toString(), out.toString());
    }
}