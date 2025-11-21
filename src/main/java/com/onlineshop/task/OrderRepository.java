package com.onlineshop.task;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class OrderRepository {
    private final ConcurrentMap<String, Order> store = new ConcurrentHashMap<>();

    public Order save(Order order) {
        store.put(order.getId(), order);
        return order;
    }

    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public Collection<Order> findAll() {
        return store.values();
    }

    public Optional<Order> delete(String id) {
        return Optional.ofNullable(store.remove(id));
    }

    public void clear() {
        store.clear();
    }
}
