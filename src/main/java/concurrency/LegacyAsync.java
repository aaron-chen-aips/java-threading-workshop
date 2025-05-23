package concurrency;

import java.util.concurrent.CompletableFuture;

public class LegacyAsync {
    /**
     * Wrap a thread in a CompletableFuture.
     */
    public static CompletableFuture<String> computeInThread(String name) {
        CompletableFuture<String> future = new CompletableFuture<>();

        new Thread(() -> {
            try {
                Thread.sleep(500);
                String result = "Hello, " + name;
                future.complete(result);
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            }
        }, "Worker-" + name).start();

        return future;
    }

    public static void main(String[] args) {
        CompletableFuture<String> f1 = computeInThread("Alice");
        CompletableFuture<String> f2 = computeInThread("Bob");
        CompletableFuture<String> f3 = computeInThread("Carol");

        CompletableFuture
                .allOf(f1, f2, f3)
                .thenRun(() -> {
                    String out = f1.join() + " | " + f2.join() + " | " + f3.join();
                    System.out.println("Combined: " + out);
                })
                .exceptionally(ex -> {
                    System.err.println(ex.getMessage());
                    return null;
                });
    }
}