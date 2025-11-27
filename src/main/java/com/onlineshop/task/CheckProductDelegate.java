package com.onlineshop.task;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
@Named("checkProductDelegate")
public class CheckProductDelegate implements JavaDelegate {

    private static final Logger LOG = Logger.getLogger(CheckProductDelegate.class.getName());

    @Inject
    InventoryRepository inventoryRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String productId = (String) execution.getVariable("productId");
        Object qVar = execution.getVariable("quantity");

        int quantity;
        try {
            if (qVar instanceof Number) {
                quantity = ((Number) qVar).intValue();
            } else if (qVar instanceof String) {
                quantity = Integer.parseInt(((String) qVar).trim());
            } else {
                throw new NumberFormatException("unsupported quantity type");
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Invalid quantity variable: {0}", qVar);
            throw new BpmnError("INVALID_PRODUCT");
        }

        if (productId == null || productId.isBlank() || !inventoryRepository.isValidProduct(productId)) {
            LOG.log(Level.FINE, "Invalid product: {0}", productId);
            throw new BpmnError("INVALID_PRODUCT");
        }

        int available = inventoryRepository.getQuantity(productId);
        boolean reserved = false;
        if (quantity > 0 && available >= quantity) {
            reserved = inventoryRepository.reserveIfAvailable(productId, quantity);
        }

        execution.setVariable("availableQuantity", available);
        execution.setVariable("inventoryReserved", reserved);
        if (reserved) {
            execution.setVariable("reservedQuantity", quantity);
        }

        LOG.info("CheckProductDelegate executed: product=" + productId + ", qty=" + quantity + ", reserved=" + reserved);
    }
}
