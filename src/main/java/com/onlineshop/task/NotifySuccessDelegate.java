package com.onlineshop.task;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("notifySuccessDelegate")
@ApplicationScoped
public class NotifySuccessDelegate implements JavaDelegate {
    private static final Logger LOG = Logger.getLogger(NotifySuccessDelegate.class.getName());

    @Inject
    OrderRepository orderRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String orderId = (String) execution.getVariable("orderId");
        if (orderId == null || orderId.isBlank()) {
            LOG.warning("NotifySuccessDelegate: missing orderId");
            return;
        }

        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isPresent()) {
            Order order = opt.get();
            order.setStatus("NOTIFIED");
            orderRepository.save(order);
            LOG.log(Level.INFO, "NotifySuccessDelegate: notified customer for order {0}", orderId);
        } else {
            LOG.log(Level.WARNING, "NotifySuccessDelegate: order not found {0}", orderId);
        }
    }
}
