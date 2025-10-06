package com.example.lb2;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class IpHashingLoadBalancer implements LoadBalancer {
    @Override
    public Server select(List<Server> servers, String key) {
        if (servers == null || servers.isEmpty()) return null;
        byte[] bytes = (key == null ? "" : key).getBytes(StandardCharsets.UTF_8);
        int hash = 0;
        for (byte b : bytes) {
            hash = (hash * 31) + (b & 0xff);
        }
        int idx = Math.floorMod(hash, servers.size());
        return servers.get(idx);
    }
}
