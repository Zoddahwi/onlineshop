package com.onlineshop.task;

public class Order {
    private String id;
    private String customer;
    private double total;
    private String status;

    public Order() {}

    public Order(String id, String customer, double total, String status) {
        this.id = id;
        this.customer = customer;
        this.total = total;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Order{id='" + id + "', customer='" + customer + "', total=" + total + ", status='" + status + "'}";
    }
}