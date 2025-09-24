package com.example.lb.simple_load_balancer.strategy;

import com.example.lb.simple_load_balancer.Server;

import java.util.List;
import java.util.Map;

public interface Strategy {

    public Server apply(Map<String, Server> servers) throws Exception;
}
