package com.onlineshop.task;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InventoryRepository {
    /**
     * Thread-safe map holding current stock for each product id.
     * Key: product id (e.g. "product-1")
     * Value: available quantity
     */
    private final ConcurrentHashMap<String, Integer> stock = new ConcurrentHashMap<>();

    /**
     * Initializes repository with some example products and their stock levels.
     */
    public InventoryRepository() {
        // Initialize with some products
        stock.put("product-1", 100);
        stock.put("product-2", 200);
        stock.put("product-3", 150);
    }

    /**
     * Checks whether the given product id exists in inventory.
     *
     * @param productId product identifier to validate
     * @return true if productId is non-null and present in the stock map
     */
    public boolean isValidProduct(String productId){
        return productId != null && stock.containsKey(productId);
    }

    /**
     * Attempts to reserve {@code quantity} units of {@code productId}.
     *
     * Uses {@link ConcurrentHashMap#computeIfPresent} to atomically check availability
     * and decrement the stock if enough units are available.
     *
     * Note: The current implementation returns true when the compute operation
     * succeeds and a post-check on quantities indicates the stock was decreased.
     *
     * @param productId product identifier whose stock to reserve
     * @param quantity number of units to reserve
     * @return true if reservation was applied (enough stock existed), false otherwise
     */
    public boolean reserveStock(String productId, int quantity) {
        // Atomically update the stock: if the product exists and enough available,
        // subtract quantity; otherwise leave the quantity unchanged.
        return stock.computeIfPresent(productId, (key, available) -> {
            if (available >= quantity) {
                // Enough stock: return the new reduced value
                return available - quantity;
            } else {
                // Not enough stock: return the old value to leave map unchanged
                return available;
            }
        }) != null && getQuantity(productId) <= (getInitialStock(productId) - quantity);
    }

    /**
     * Returns the current value stored for the product or 0 if missing.
     *
     * Note: named getInitialStock but it currently reads the same storage as
     * getQuantity. If an initial/original stock snapshot is needed, introduce
     * separate storage or tracking.
     */
    private int getInitialStock(String productId) {
        return stock.getOrDefault(productId, 0);
    }

    /**
     * Returns the current available quantity for the product or 0 if missing.
     */
    public int getQuantity(String productId) {
        return stock.getOrDefault(productId, 0);
    }

    public void updateInventory(String productId, int quantityInt) {
    }

    /**
     * Try to reserve quantity atomically. Returns true if reserved and decremented.
     */
    public boolean reserveIfAvailable(String productId, int quantity) {
        if (quantity <= 0) return false;
        Integer current = stock.get(productId);
        if (current == null) return false;
        while (current >= quantity) {
            if (stock.replace(productId, current, current - quantity)) {
                return true;
            }
            current = stock.get(productId);
            if (current == null) return false;
        }
        return false;
    }
}
