package com.example.lb2;

import java.util.List;

public interface LoadBalancer {
    Server select(List<Server> servers, String key);
}
