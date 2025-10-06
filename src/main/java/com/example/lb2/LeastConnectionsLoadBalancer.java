package com.example.lb2;

import java.util.Comparator;
import java.util.List;

public class LeastConnectionsLoadBalancer implements LoadBalancer {
    @Override
    public Server select(List<Server> servers, String key) {
        if (servers == null || servers.isEmpty()) return null;
        return servers.stream()
                .min(Comparator.comparingInt(Server::getActiveConnections))
                .orElse(null);
    }
}
