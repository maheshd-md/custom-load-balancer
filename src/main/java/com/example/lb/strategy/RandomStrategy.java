package com.example.lb.strategy;

import com.example.lb.Server;

import java.util.*;

public class RandomStrategy implements Strategy {
    private final Random random = new Random();

    @Override
    public Server apply(List<Server> servers) throws Exception {
        List<Server> availableServers = servers.stream()
                .filter(s -> s.getCurrentLoad() < s.getCapacity())
                .toList();
        return availableServers.get(random.nextInt(availableServers.size()));
    }
}
