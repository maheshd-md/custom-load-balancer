package com.example.lb2;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private final String id;
    private final String ip;
    private final int capacity;
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    public Server(String id, String ip, int capacity) {
        this.id = id;
        this.ip = ip;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getCapacity() {
        return capacity;
    }

    public int incrementConnections() {
        return activeConnections.incrementAndGet();
    }

    public int decrementConnections() {
        return activeConnections.updateAndGet(v -> Math.max(0, v - 1));
    }

    public int getActiveConnections() {
        return activeConnections.get();
    }

    public boolean acceptConnection() {
        while (true) {
            int current = activeConnections.get();
            if (current >= capacity) return false;
            if (activeConnections.compareAndSet(current, current + 1)) return true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Server server = (Server) o;
        return Objects.equals(id, server.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}


