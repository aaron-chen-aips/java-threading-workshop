package concurrency;

import java.util.concurrent.atomic.AtomicInteger;


class ThreadUnsafeCounter {
    private int counter = 0;

    public void add() {
        counter++;
    }

    public int get() {
        return counter;
    }
}

class AtomicCounter {
    private final AtomicInteger counter = new AtomicInteger(0);

    public void add() {
        counter.getAndAdd(1);
    }

    public int get() {
        return counter.get();
    }
}

class SynchronizedCounter {
    private int counter = 0;

    public synchronized void add() {
        counter++;
    }

    public synchronized int get() {
        return counter;
    }
}

class VolatileCounter {
    private volatile int counter = 0;

    public void add() {
        counter++;
    }

    public int get() {
        return counter;
    }
}