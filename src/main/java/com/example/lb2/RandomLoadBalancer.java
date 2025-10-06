package com.example.lb2;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalancer implements LoadBalancer {
    @Override
    public Server select(List<Server> servers, String key) {
        if (servers == null || servers.isEmpty()) return null;
        int idx = ThreadLocalRandom.current().nextInt(servers.size());
        return servers.get(idx);
    }
}
