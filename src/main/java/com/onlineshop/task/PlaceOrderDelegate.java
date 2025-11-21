package com.onlineshop.task;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("placeOrderDelegate")
@ApplicationScoped
public class PlaceOrderDelegate implements JavaDelegate {
    private static final Logger LOG = Logger.getLogger(PlaceOrderDelegate.class.getName());

    @Inject
    OrderRepository orderRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String orderId = (String) execution.getVariable("orderId");
        if (orderId == null || orderId.isBlank()) {
            LOG.warning("PlaceOrderDelegate: missing orderId");
            execution.setVariable("orderPlaced", false);
            return;
        }

        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isEmpty()) {
            LOG.log(Level.WARNING, "PlaceOrderDelegate: order not found {0}", orderId);
            execution.setVariable("orderPlaced", false);
            return;
        }

        Order order = opt.get();
        order.setStatus("PLACED");
        orderRepository.save(order);

        execution.setVariable("orderPlaced", true);
        LOG.log(Level.INFO, "Order placed: {0}", orderId);
    }
}
