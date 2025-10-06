package com.example.lb2;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public Server select(List<Server> servers, String key) {
        if (servers == null || servers.isEmpty()) return null;
        int idx = Math.floorMod(counter.getAndIncrement(), servers.size());
        return servers.get(idx);
    }
}
