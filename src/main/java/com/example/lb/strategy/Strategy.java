package com.example.lb.strategy;

import com.example.lb.Server;

import java.util.List;

public interface Strategy {
    Server apply(List<Server> servers) throws Exception;

}
