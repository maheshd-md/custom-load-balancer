package com.example.lb.strategy;

import com.example.lb.Server;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinStrategy implements Strategy {
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public Server apply(List<Server> servers) throws Exception {
        List<Server> availableServers = servers.stream()
                .filter(s -> s.getCurrentLoad() < s.getCapacity())
                .toList();
        int currentIndex = index.getAndUpdate(i -> (i + 1) % availableServers.size());
        return availableServers.get(currentIndex);
    }
}
