package asyncdemo;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {
    public static void main(String[] args) throws Exception {
        virtualThreadRun();
    }

    static void fixedPoolRun() throws InterruptedException {
        int tasks = 10_000;
        Instant t0 = Instant.now();

        ExecutorService pool = Executors.newFixedThreadPool(tasks);

        for (int i = 0; i < tasks; i++) {
            pool.submit(() -> {
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
        }

        Instant t1 = Instant.now();
        log.info("FixedPool submit time: " +
                Duration.between(t0, t1).toMillis() + " ms");

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.MINUTES);
    }

    static void virtualThreadRun() throws InterruptedException {
        int tasks = 10_000;
        Instant t0 = Instant.now();

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < tasks; i++) {
                pool.submit(() -> {
                    try {
                        Thread.sleep(1_000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                });
            }
            Instant t1 = Instant.now();
            System.out.println("VirtualThread submit time: " +
                    Duration.between(t0, t1).toMillis() + " ms");

            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.MINUTES);
        }
    }
}