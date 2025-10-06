package com.example.lb;

import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private final String ip;
    private final int capacity;
    private final AtomicInteger currentLoad = new AtomicInteger(0);

    public Server(String ip, int capacity) {
        this.ip = ip;
        this.capacity = capacity;
    }

    public String getIp() {
        return ip;
    }


    public int getCapacity() {
        return capacity;
    }

    public int getCurrentLoad() {
        return currentLoad.get();
    }

    public void incrementLoad() {
        currentLoad.incrementAndGet();
    }

    public void decrementLoad() {
        currentLoad.decrementAndGet();
    }

}
