package com.example.lb.strategy;

import com.example.lb.Server;

import java.util.List;

public class IpHashingStrategy implements Strategy {
    private final String clientIp;

    public IpHashingStrategy(String clientIp) {
        this.clientIp = clientIp;
    }

    @Override
    public Server apply(List<Server> servers) {
        int hash = clientIp.hashCode();
        return servers.get(Math.abs(hash) % servers.size());
    }
}
