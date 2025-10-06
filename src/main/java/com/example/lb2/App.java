package com.example.lb2;

import java.util.concurrent.CompletableFuture;

public class App {
    public static void main(String[] args) throws Exception {
        InMemoryStorage storage = new InMemoryStorage();
        storage.addServer(new Server("s1", "10.0.0.1", 5));
        storage.addServer(new Server("s2", "10.0.0.2", 5));
        storage.addServer(new Server("s3", "10.0.0.3", 5));

        CustomLoadBalancerService service = new CustomLoadBalancerService(storage);
        service.register("iphash", new IpHashingLoadBalancer());
        service.register("random", new RandomLoadBalancer());
        service.register("rr", new RoundRobinLoadBalancer());
        service.register("least", new LeastConnectionsLoadBalancer());

        CompletableFuture<Void> f1 = service.handleRequest("rr", null, () -> System.out.println("Handled by RR")).thenAccept(s -> System.out.println("RR -> " + s));
        CompletableFuture<Void> f2 = service.handleRequest("iphash", "192.168.1.10", () -> System.out.println("Handled by IPHASH")).thenAccept(s -> System.out.println("IPHASH -> " + s));
        CompletableFuture<Void> f3 = service.handleRequest("random", null, () -> System.out.println("Handled by RANDOM")).thenAccept(s -> System.out.println("RANDOM -> " + s));
        CompletableFuture<Void> f4 = service.handleRequest("least", null, () -> System.out.println("Handled by LEAST")).thenAccept(s -> System.out.println("LEAST -> " + s));
        CompletableFuture.allOf(f1, f2, f3, f4).join();
        service.shutdown();
        System.out.println("All requests processed.");
    }
}
