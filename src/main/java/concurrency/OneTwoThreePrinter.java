package concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

class OnTwoThreePrinterThreadUnsafe {
    public void first(Runnable printFirst) throws InterruptedException {
        printFirst.run();
    }
    public void second(Runnable printSecond) throws InterruptedException {
        printSecond.run();
    }
    public void third(Runnable printThird) throws InterruptedException {
        printThird.run();
    }
}

class OneTwoThreePrinterWithLatch {
    private final CountDownLatch l1 = new CountDownLatch(1);
    private final CountDownLatch l2 = new CountDownLatch(1);

    public void first(Runnable printFirst) throws InterruptedException {
        printFirst.run();
        l1.countDown();
    }
    public void second(Runnable printSecond) throws InterruptedException {
        l1.await();
        printSecond.run();
        l2.countDown();
    }
    public void third(Runnable printThird) throws InterruptedException {
        l2.await();
        printThird.run();
    }
}

class OneTwoThreePrinterWithSemaphore {
    private final Semaphore s1 = new Semaphore(0);
    private final Semaphore s2 = new Semaphore(0);

    public void first(Runnable printFirst) throws InterruptedException {
        printFirst.run();
        s1.release();
    }
    public void second(Runnable printSecond) throws InterruptedException {
        s1.acquire();
        printSecond.run();
        s2.release();
    }
    public void third(Runnable printThird) throws InterruptedException {
        s2.acquire();
        printThird.run();
    }
}
