package com.example.lb2;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancerTest2 {
    private static InMemoryStorage storage;
    private static CustomLoadBalancerService service;

    @BeforeAll
    public static void setup() {
        storage = new InMemoryStorage();
        for (int i = 0; i < 5; i++) {
            storage.addServer(new Server("srv-" + i, "10.0.0." + i, 10));
        }
        service = new CustomLoadBalancerService(storage);
        service.register("iphash", new IpHashingLoadBalancer());
        service.register("random", new RandomLoadBalancer());
        service.register("rr", new RoundRobinLoadBalancer());
        service.register("least", new LeastConnectionsLoadBalancer());
    }

    @AfterAll
    public static void tearDown() {
        service.shutdown();
    }

    @Test
    public void testRoundRobinDistribution() throws Exception {
        List<Server> chosen = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Server s = service.handleRequest("rr", null, () -> {
            }).get(1, TimeUnit.SECONDS);
            chosen.add(s);
        }
        Assertions.assertEquals(5, chosen.stream().map(Server::getId).distinct().count());
    }

    @Test
    public void testIpHashingStability() throws Exception {
        String key = "192.168.1.100";
        Server a = service.handleRequest("iphash", key, () -> {
        }).get(1, TimeUnit.SECONDS);
        Server b = service.handleRequest("iphash", key, () -> {
        }).get(1, TimeUnit.SECONDS);
        Assertions.assertEquals(a.getId(), b.getId());
    }

    @Test
    public void testLeastConnectionsPrefersLessLoaded() throws Exception {
        Server s0 = storage.getServer("srv-0");
        for (int i = 0; i < 5; i++) s0.incrementConnections();
        Server chosen = service.handleRequest("least", null, () -> {
        }).get(1, TimeUnit.SECONDS);
        Assertions.assertNotEquals("srv-0", chosen.getId());
        for (int i = 0; i < 5; i++) s0.decrementConnections();
    }

    @Test
    public void testMaxTenConcurrentRequests() throws Exception {
        int totalRequests = 50;
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        AtomicInteger currentConcurrent = new AtomicInteger(0);
        ExecutorService exec = Executors.newFixedThreadPool(20);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            CompletableFuture<Void> f = new CompletableFuture<>();
            futures.add(f);
            exec.submit(() -> {
                try {
                    startLatch.await();
                    service.handleRequest("random", null, () -> {
                        int now = currentConcurrent.incrementAndGet();
                        maxConcurrent.updateAndGet(prev -> Math.max(prev, now));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {
                        }
                        currentConcurrent.decrementAndGet();
                    }).whenComplete((s, ex) -> {
                        if (ex != null) f.completeExceptionally(ex);
                        else f.complete(null);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        startLatch.countDown();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);
        exec.shutdownNow();

        Assertions.assertTrue(maxConcurrent.get() <= 10, "More than 10 concurrent requests observed: " + maxConcurrent.get());
    }

    @Test
    public void testServerCapacityRespected() throws Exception {
        Server s = new Server("tiny", "127.0.0.1", 2);
        storage.addServer(s);

        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture<Server> f1 = service.handleRequest("random", null, () -> {
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }
        });
        CompletableFuture<Server> f2 = service.handleRequest("random", null, () -> {
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }
        });
        CompletableFuture<Server> f3 = service.handleRequest("random", null, () -> {
        });

        latch.countDown();

        Server r1 = f1.get(2, TimeUnit.SECONDS);
        Server r2 = f2.get(2, TimeUnit.SECONDS);
        Server r3 = f3.get(2, TimeUnit.SECONDS);

        Assertions.assertNotNull(r1);
        Assertions.assertNotNull(r2);
        Assertions.assertNotNull(r3);

        storage.removeServer("tiny");
    }
}