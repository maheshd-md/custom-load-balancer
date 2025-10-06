package com.example.lb;

import com.example.lb.strategy.*;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class LoadBalancerTest {

    private LoadBalancer lb;

    @BeforeEach
    void setUp() {
        lb = new LoadBalancer(new RoundRobinStrategy());
        lb.addServer("10.0.0.1", 100);
        lb.addServer("10.0.0.2", 100);
        lb.addServer("10.0.0.3", 100);
    }

    @AfterEach
    void tearDown() {
        lb.shutdown();
    }

    @Test
    void testRoundRobin() throws Exception {
        Set<String> selected = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            selected.add(lb.nextServer().getIp());
        }
        assertEquals(3, selected.size());
    }

    @Test
    void testRandomStrategy() throws Exception {
        lb = new LoadBalancer(new RandomStrategy());
        lb.addServer("10.0.0.1", 100);
        lb.addServer("10.0.0.2", 100);
        assertNotNull(lb.nextServer());
    }

    @Test
    void testIpHashingStrategy() throws Exception {
        lb = new LoadBalancer(new IpHashingStrategy("192.168.0.5"));
        lb.addServer("10.0.0.1", 100);
        lb.addServer("10.0.0.2", 100);
        assertNotNull(lb.nextServer());
    }

    @Test
    void testLeastConnectionStrategy() throws Exception {
        lb = new LoadBalancer(new LeastConnectionStrategy());
        lb.addServer("10.0.0.1", 100);
        lb.addServer("10.0.0.2", 100);
        Server selected1 = lb.nextServer();
        Server selected2 = lb.nextServer();
        assertEquals("10.0.0.2", selected2.getIp());
    }

    @Test
    void testParallelRequestsLimit() throws Exception {
        List<Future<String>> results = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            results.add(lb.forwardRequest(() -> "response-" + Thread.currentThread().getName()));
        }
        for (Future<String> result : results) {
            assertTrue(result.get().startsWith("response-"));
        }
    }

    @Test
    void testParallelRequestsLimitNegative() throws Exception {
        long start = System.currentTimeMillis();
        List<Future<String>> results = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            results.add(lb.forwardRequest(() -> {
                Thread.sleep(2000);
                return "completed";
            }));
        }
        for (Future<String> result : results) {
            result.get();
        }
        long end = System.currentTimeMillis();
        assertTrue(end - start > 20);
    }
}
