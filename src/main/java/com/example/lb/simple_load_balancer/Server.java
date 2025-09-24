package com.example.lb.simple_load_balancer;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Server {

    private String ip;
    private final Integer loadCapacity;
    private AtomicInteger currentLoad;
    private AtomicInteger availableLoad;

    public Server(String ip, Integer loadCapacity) {
        this.ip = ip;
        this.loadCapacity = loadCapacity;
    }
}
