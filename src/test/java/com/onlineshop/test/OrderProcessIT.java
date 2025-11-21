package com.onlineshop.test;

import io.quarkus.test.junit.QuarkusTest;
import com.onlineshop.task.InventoryRepository;
import com.onlineshop.task.Order;
import com.onlineshop.task.OrderRepository;
import io.quarkus.test.InjectMock;
import static org.mockito.Mockito.*;
import jakarta.inject.Inject;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@QuarkusTest
public class OrderProcessIT {


    @Inject
    ProcessEngine processEngine;

    @InjectMock
    InventoryRepository inventoryRepository;

    @InjectMock
    OrderRepository orderRepository;

    // helper: wait up to timeoutMs for a variable to appear on the execution (simple poll)
    private Object waitForVariable(RuntimeService runtimeService, String executionId, String varName, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            Object v = runtimeService.getVariable(executionId, varName);
            if (v != null) return v;
            Thread.sleep(100);
        }
        return runtimeService.getVariable(executionId, varName);
    }

    @Test
    public void testOrderHappyPath() throws Exception {
        // arrange
        when(inventoryRepository.isValidProduct("p1")).thenReturn(true);
        when(inventoryRepository.getQuantity("p1")).thenReturn(5);
        when(inventoryRepository.reserveIfAvailable("p1", 2)).thenReturn(true);

        Order order = new Order();
        order.setId("o1");
        order.setStatus("CREATED");
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String, Object> vars = new HashMap<>();
        vars.put("productId", "p1");
        vars.put("quantity", 2);
        vars.put("orderId", "o1");

        // act
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("orderProcess", vars);
        assertNotNull(pi);
        String pid = pi.getId();

        // wait for process to create variables the delegate writes
        Object reserved = waitForVariable(runtimeService, pid, "inventoryReserved", 5000);
        assertNotNull(reserved, "inventoryReserved should be set by CheckProductDelegate");
        assertTrue(reserved instanceof Boolean && ((Boolean) reserved), "inventoryReserved should be true");

        Object available = runtimeService.getVariable(pid, "availableQuantity");
        assertNotNull(available);
        assertEquals(5, ((Number) available).intValue());

        // verify order repository was used by PlaceOrderDelegate
        // PlaceOrderDelegate sets order status and saves; verify save called when the process reaches that task
        // allow some time for the PlaceOrderDelegate to run
        Thread.sleep(250);
        verify(orderRepository, atLeastOnce()).save(any());
    }

    @Test
    public void testInvalidProductPath() throws Exception {
        // arrange: invalid product triggers BpmnError and alternate flow
        when(inventoryRepository.isValidProduct("bad")).thenReturn(false);

        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String, Object> vars = new HashMap<>();
        vars.put("productId", "bad");
        vars.put("quantity", 1);
        vars.put("orderId", "o2");

        // act
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("orderProcess", vars);
        assertNotNull(pi);
        String pid = pi.getId();

        // wait a short while for the boundary/error flow to execute
        Thread.sleep(500);

        // inventoryReserved should not be true for invalid product
        Object reserved = runtimeService.getVariable(pid, "inventoryReserved");
        if (reserved != null) {
            assertFalse((reserved instanceof Boolean) && ((Boolean) reserved), "inventoryReserved should be false or absent for invalid product");
        }

        // process may follow the error path; verify OrderRepository.save is not called
        verify(orderRepository, never()).save(any());
    }
}
