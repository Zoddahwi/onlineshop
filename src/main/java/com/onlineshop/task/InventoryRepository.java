package com.onlineshop.task;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class InventoryRepository {

    private final Map<String, AtomicInteger> stock = new ConcurrentHashMap<>();

    public InventoryRepository() {
        stock.put("SKU-1234", new AtomicInteger(10));
        stock.put("SKU-5678", new AtomicInteger(5));
    }

    public boolean isValidProduct(String productId) {
        return stock.containsKey(productId);
    }

    public int getQuantity(String productId) {
        AtomicInteger a = stock.get(productId);
        return a == null ? 0 : a.get();
    }

    public boolean reserveIfAvailable(String productId, int qty) {
        AtomicInteger a = stock.get(productId);
        if (a == null) return false;
        while (true) {
            int current = a.get();
            if (current < qty) return false;
            if (a.compareAndSet(current, current - qty)) return true;
        }
    }
}
