package concurrency;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ThreadSafeCounterTest {
    int THREAD_COUNT = 5_000;

    @Test
    void testThreadSafeCounter() throws InterruptedException {
        final var counter = new AtomicCounter();

        final CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);
        final ExecutorService executorService = Executors.newCachedThreadPool();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> {
                counter.add();
                countDownLatch.countDown();
            });
        }
        executorService.shutdown();
        countDownLatch.await();


        final int actual = counter.get();

        System.out.println("Using counter type:" + counter.getClass().getSimpleName());
        System.out.println("Expected: " + THREAD_COUNT);
        System.out.println("Actual: " + actual);
        assertEquals(THREAD_COUNT, actual);
    }
}