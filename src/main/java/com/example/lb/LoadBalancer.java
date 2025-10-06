package com.example.lb;

import com.example.lb.strategy.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LoadBalancer {

    private final List<Server> servers = new ArrayList<>();
    private final Strategy strategy;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final Lock lock = new ReentrantLock();

    public LoadBalancer(Strategy strategy) {
        this.strategy = strategy;
    }

    public void addServer(String ip, int capacity) {
        lock.lock();
        if (servers.stream().noneMatch(server -> server.getIp().equals(ip))) {
            servers.add(new Server(ip, capacity));
        }
        lock.unlock();
    }

    public void removeServer(String ip) {
        lock.lock();
        servers.removeIf(server -> server.getIp().equals(ip));
        lock.unlock();
    }

    public Server nextServer() throws Exception {
        lock.lock();
        Server server = strategy.apply(getAvailableServers());
        server.incrementLoad();
        lock.unlock();
        return server;
    }

    public <T> Future<T> forwardRequest(Callable<T> requestTask) throws Exception {
        if (servers.isEmpty()) throw new Exception("No available servers.");
        return executor.submit(() -> {
            Server server = null;
            try {
                server = nextServer();
                return requestTask.call();
            } finally {
                if (null != server) {
                    server.decrementLoad();
                }
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }

    public List<Server> getAllServers() {
        return servers;
    }

    List<Server> getAvailableServers() throws Exception {
        List<Server> availableServers = servers.stream()
                .filter(server -> server.getCurrentLoad() < server.getCapacity())
                .toList();

        if (availableServers.isEmpty()) {
            throw new Exception("No server available");
        }
        return availableServers;
    }
}
