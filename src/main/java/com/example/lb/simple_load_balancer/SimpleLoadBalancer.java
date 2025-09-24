package com.example.lb.simple_load_balancer;

import com.example.lb.simple_load_balancer.strategy.Strategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SimpleLoadBalancer {

    private Map<String, Server> servers;

    @Autowired
    RestTemplate restTemplate;

    public SimpleLoadBalancer() {
        servers = new HashMap<>();
    }

    public void addServer(String ip, Integer loadCapacity) {
        servers.put(ip, new Server(ip, loadCapacity));
    }

    public void removeServer(String ip) {
        servers.remove(ip);
    }

    public Server nextServer(Strategy strategy) throws Exception {
        Server server = strategy.apply(servers);
        server.getAvailableLoad().incrementAndGet();
        return server;
    }

    public ResponseEntity<?> forwardRequest(
            String path, HttpMethod method, RequestEntity<?> requestEntity, Strategy strategy) throws Exception {
        Server server = nextServer(strategy);
        URI uri = new URI("https://" + server.getIp() + "/" + path);
        try {
            ResponseEntity<?> response = restTemplate.exchange(uri, method, requestEntity, String.class);
            return response;
        } finally {
            server.getAvailableLoad().decrementAndGet();
        }
    }

}
