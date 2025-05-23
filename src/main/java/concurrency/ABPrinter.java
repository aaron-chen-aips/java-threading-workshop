package concurrency;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

class ABPrinter {

    private final int n;

    public ABPrinter(int n) {
        this.n = n; // number of pairs, e.g., 10 for 10 pairs of A and B
    }

    public void a(Runnable printA) throws InterruptedException {
        printA.run();
    }

    public void b(Runnable printB) throws InterruptedException {
        printB.run();
    }

}