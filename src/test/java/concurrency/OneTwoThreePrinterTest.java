package concurrency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OneTwoThreePrinterTest {

    @Test
    void testOneTwoThreePrinter() throws InterruptedException {
        int[][] inputsOrders = {{1,2,3},{1,3,2},{2,1,3},{2,3,1},{3,1,2},{3,2,1}};

        for(int[] order: inputsOrders){
            final var printer = new OneTwoThreePrinterWithLatch();
            StringBuffer out = new StringBuffer();

            Thread t1 = new Thread(() -> {
                try { printer.first(() -> out.append("first")); }
                catch (InterruptedException ignored) { }
            });
            Thread t2 = new Thread(() -> {
                try { printer.second(() -> out.append("second")); }
                catch (InterruptedException ignored) { }
            });
            Thread t3 = new Thread(() -> {
                try { printer.third(() -> out.append("third")); }
                catch (InterruptedException ignored) { }
            });

            for (int n : order) {
                if (n == 1) t1.start();
                if (n == 2) t2.start();
                if (n == 3) t3.start();
            }
            t1.join(); t2.join(); t3.join();

            System.out.println("Order: " + order[0] + order[1] + order[2]);
            System.out.println("Output: " + out);
            assert out.toString().equals("firstsecondthird");
        }
    }
}