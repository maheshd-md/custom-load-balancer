package com.example.lb2;

import com.example.lb2.Server;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStorage {
    private final Map<String, Server> servers = new ConcurrentHashMap<>();

    public void addServer(Server server) {
        servers.put(server.getId(), server);
    }

    public void removeServer(String id) {
        servers.remove(id);
    }

    public Server getServer(String id) {
        return servers.get(id);
    }

    public Collection<Server> getAll() {
        return servers.values();
    }

    public int size() {
        return servers.size();
    }
}
    