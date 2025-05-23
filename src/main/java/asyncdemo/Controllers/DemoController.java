package asyncdemo.Controllers;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.mutiny.core.Vertx;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;
import io.smallrye.common.annotation.RunOnVirtualThread;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Flow;
import java.util.concurrent.ThreadLocalRandom;

@Path("/demo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class DemoController {

    @Inject
    Vertx vertx;

    @GET
    @Path("/sync")
    public String sync() {
        String key  = mockApi1();
        String data = mockApi2();
        String db_data = mockDb(key);
        log.info("Sync result: {}", "data=" + data + "; db_data=" + db_data);
        return "data=" + data + "; db_data=" + db_data;
    }

    @GET
    @Path("/real-async")
    public Uni<String> reactiveNonBlocking() {
        Uni<String> dbCall = mockApi1Async().flatMap(this::mockDbAsync);
//        Uni<String> api2Call = mockApi2Async();
        Uni<String> api2Call = mockApi2AsyncOnWorkerThread();
        return Uni.combine()
                .all()
                .unis(api2Call, dbCall)
                .with((api2, db) ->
                        "data=" + api2 + "; db_data=" + db
                ).invoke(
                        result -> log.info("Real async result: {}", result)
                );
    }

    @GET
    @Path("/fake-async")
    public Uni<String> reactive() {
        Uni<String> api2Call = Uni.createFrom().item(this::mockApi2);
        Uni<String> dbUni = Uni.createFrom().item(this::mockApi1)
                .flatMap(key -> Uni.createFrom().item(() -> mockDb(key)));

        return Uni.combine()
                .all()
                .unis(api2Call, dbUni)
                .with((api2Response, dbResponse) ->
                        "data=" + api2Response + "; db_data=" + dbResponse
                ).invoke(
                        result -> log.info("Fake async result: {}", result)
                );
    }

    @GET
    @Path("/virtual")
    @RunOnVirtualThread
    public String virtualEndpoint() {
        String key  = mockApi1();
        String data = mockApi2AsyncOnWorkerThread().await().indefinitely();
        String db_data = mockDb(key);
        log.info("Virtual result: {}", "data=" + data + "; db_data=" + db_data);
        return "data=" + data + "; db_data=" + db_data;
    }

    @GET
    @Path("/pinned-virtual")
    @RunOnVirtualThread
    public String pinnedVirtual() throws InterruptedException {
        Object monitor = new Object();
        synchronized(monitor) {
            Thread.sleep(200); // The virtual thread cannot be unmounted because it holds a lock,
            // so the carrier thread is blocked.
        }
        return "response";
    }

    @GET
    @Path("/bp")
    public void backpressure(){
        log.info("Long.max" + Long.MAX_VALUE);
        Multi.createFrom().range(0, 10)
                .onSubscription().invoke(sub -> log.info("Received subscription: " + sub))
                .onRequest().invoke(req -> log.info("onRequest: " + req))
                .onItem().transform(i -> i * 100)
                .subscribe().with(item -> log.info("i: " + item),
                        Throwable::printStackTrace,
                        () -> log.info("Completed")
                );
    }

    @GET
    @Path("/real-bp")
    public void realBp(){
        Multi<Integer> multi = Multi.createFrom().range(0, 10)
                .onItem().invoke(i -> log.info("raw item: " + i))
                .onSubscription().invoke(sub -> log.info("onSubscription: " + sub))
                .onRequest().invoke(req -> log.info("onRequest: " + req))
                .onItem().transform(i -> i * 100);

        // 6. Subscribe with a Flow.Subscriber to demonstrate back-pressure
        multi.subscribe(new Flow.Subscriber<>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription s) {
                this.subscription = s;
                log.info("Requesting....");
                subscription.request(1);  // request the first item
            }

            @Override
            public void onNext(Integer item) {
                log.info("Received item: " + item);
                int n = 1;
//                int n = ThreadLocalRandom.current().nextInt(1, 6); // random number between 1 and 5
                log.info("Requesting {} more item", n);
                subscription.request(n);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {
                log.info("Stream completed");  // completion signal
            }
        });
    }


    private String mockApi1() {
        try {
            log.info("calling api1");
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "api1 - response";
    }

    private String mockApi2() {
        try {
            log.info("calling api2");
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "api2 - response";
    }

    private String mockDb(String key) {
        try {
            log.info("searching db for key: {}", key);
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "dbInfoFor:" + key;
    }

    private Uni<String> mockApi1Async() {
        return timerUni(50, "api1 - response")
                .onItem()
                .invoke(key -> log.info("calling api1"));
    }


    private Uni<String> mockApi2Async() {
        return timerUni(100, "api2-response")
                .onItem()
                .invoke(key -> log.info("calling api2"));
    }

    private Uni<String> mockApi2AsyncOnWorkerThread() {
        return Uni.createFrom().item(() -> {
                    log.info("calling api2 and doing some computationally expensive task");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return "api2-response";
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    private Uni<String> mockDbAsync(String key) {
        return timerUni(50, "dbInfoFor:" + key)
                .onItem()
                .invoke(dbInfo -> log.info("searching db for key: {}", key));
    }


    private Uni<String> timerUni(long delayMs, String result) {
        return Uni.createFrom().emitter(em -> vertx.getDelegate().setTimer(delayMs, id -> em.complete(result)));
    }
}


