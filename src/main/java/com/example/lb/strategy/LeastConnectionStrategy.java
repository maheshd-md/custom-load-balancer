package com.example.lb.strategy;

import com.example.lb.Server;

import java.util.Comparator;
import java.util.List;

public class LeastConnectionStrategy implements Strategy {
    @Override
    public Server apply(List<Server> servers) throws Exception {
        return servers.stream()
                .min(Comparator.comparingInt(Server::getCurrentLoad))
                .orElseThrow(() -> new Exception("No server available"));
    }
}
