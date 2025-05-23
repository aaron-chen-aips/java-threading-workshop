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

class ABPrinterSolution {

    private final int n;
    private final AtomicInteger i;
    private final AtomicInteger j;

    private final Semaphore aSemaphore = new Semaphore(1);
    private final Semaphore bSemaphore = new Semaphore(0);

    public ABPrinterSolution(int n) {
        this.n = n;
        this.i = new AtomicInteger(0);
        this.j = new AtomicInteger(0);
    }

    public void a(Runnable printA) throws InterruptedException {
        while (i.getAndIncrement() < n) {
            aSemaphore.acquire();
            printA.run();
            bSemaphore.release();
        }
    }

    public void b(Runnable printB) throws InterruptedException {
        while (j.getAndIncrement() < n) {
            bSemaphore.acquire();
            printB.run();
            aSemaphore.release();
        }
    }
}
