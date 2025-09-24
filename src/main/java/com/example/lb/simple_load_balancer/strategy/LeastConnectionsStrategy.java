package com.example.lb.simple_load_balancer.strategy;

import com.example.lb.simple_load_balancer.Server;

import java.util.Map;

public class LeastConnectionsStrategy implements Strategy {
    @Override
    public Server apply(Map<String, Server> servers) throws Exception {
         return servers.values().stream()
                .filter(server -> server.getAvailableLoad().get() > 0)
                .min((s1, s2) -> Integer.compare(s1.getAvailableLoad().get(), s2.getAvailableLoad().get()))
                .orElseThrow(() -> new Exception("Server not available"));
    }
}
