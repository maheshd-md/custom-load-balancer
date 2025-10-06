package com.example.lb2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class CustomLoadBalancerService {
    private final InMemoryStorage storage;
    private final ConcurrentMap<String, LoadBalancer> algorithms = new ConcurrentHashMap<>();
    private final Semaphore requestSemaphore = new Semaphore(10);
    private final ExecutorService executor;

    public CustomLoadBalancerService(InMemoryStorage storage) {
        this.storage = Objects.requireNonNull(storage);
        this.executor = Executors.newCachedThreadPool();
    }

    public void register(String name, LoadBalancer lb) {
        algorithms.put(name, lb);
    }

    public List<Server> listServers() {
        return new ArrayList<>(storage.getAll());
    }

    public CompletableFuture<Server> handleRequest(String algorithmName, String key, Runnable work) {
        LoadBalancer lb = algorithms.get(algorithmName);
        if (lb == null) return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown algorithm"));

        CompletableFuture<Server> future = new CompletableFuture<>();
        executor.submit(() -> {
            boolean permitAcquired = false;
            try {
                requestSemaphore.acquire();
                permitAcquired = true;
                List<Server> servers = listServers();
                Server s = lb.select(servers, key);
                if (s == null) {
                    future.completeExceptionally(new IllegalStateException("No servers available"));
                    return;
                }
                // Try to accept connection (respect server capacity)
                boolean accepted = s.acceptConnection();
                if (!accepted) {
                    // fallback: try to find any server that can accept
                    s.decrementConnections();
                    Server fallback = servers.stream().filter(Server::acceptConnection).findFirst().orElse(null);
                    s = fallback;
                    if (s == null) {
                        future.completeExceptionally(new IllegalStateException("All servers are at capacity<"));
                        return;
                    }
                }
                try {
                    // run the user work
                    if (work != null) work.run();
                    future.complete(s);
                } finally {
                    s.decrementConnections();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.completeExceptionally(e);
            } catch (Exception e) {
                future.completeExceptionally(e);
            } finally {
                if (permitAcquired) requestSemaphore.release();
            }
        });
        return future;
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    public int availablePermits() {
        return requestSemaphore.availablePermits();
    }
}
