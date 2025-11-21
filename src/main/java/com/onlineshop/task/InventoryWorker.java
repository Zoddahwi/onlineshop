package com.onlineshop.task;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
@ApplicationScoped
public class InventoryWorker {
    private static final Logger LOG = Logger.getLogger(InventoryWorker.class.getName());
    private static final String TOPIC = "check-product";

    private ExternalTaskClient client;

    @Inject
    InventoryRepository inventoryRepository;

    @PostConstruct
    void start() {
        client = ExternalTaskClient.create()
                .baseUrl("""
                        http://localhost:8080/engine-rest""")
                .asyncResponseTimeout(10000)
                .build();

        client.subscribe(TOPIC)
                .lockDuration(10000)
                .handler(taskHandler())
                .open();

        LOG.info("InventoryWorker subscribed to topic: " + TOPIC);
    }

    private ExternalTaskHandler taskHandler() {
        return (ExternalTask externalTask, ExternalTaskService externalTaskService) -> {
            String productId = (String) externalTask.getVariable("productId");
            Object quantityVar = externalTask.getVariable("quantity");

            int quantity;
            try {
                quantity = parseQuantity(quantityVar);
            } catch (NumberFormatException ex) {
                LOG.log(Level.WARNING, "Invalid quantity for task {0}: {1}", new Object[]{externalTask.getId(), quantityVar});
                externalTaskService.handleFailure(externalTask, "Invalid quantity", ex.getMessage(), 0, 0L);
                return;
            }

            if (productId == null || productId.isBlank() || !inventoryRepository.isValidProduct(productId)) {
                LOG.log(Level.FINE, "Invalid product for task {0}: {1}", new Object[]{externalTask.getId(), productId});
                externalTaskService.handleBpmnError(externalTask, "INVALID_PRODUCT");
                return;
            }

            try {
                int available = inventoryRepository.getQuantity(productId);
                Map<String, Object> variables = new HashMap<>();
                variables.put("availableQuantity", available);

                boolean reserved = false;
                if (available >= quantity && quantity > 0) {
                    reserved = inventoryRepository.reserveIfAvailable(productId, quantity);
                }

                variables.put("inventoryReserved", reserved);
                if (reserved) {
                    variables.put("reservedQuantity", quantity);
                }

                externalTaskService.complete(externalTask, variables);
                LOG.log(Level.INFO, "Completed task {0} for product {1}: reserved={2}", new Object[]{externalTask.getId(), productId, reserved});
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error processing external task " + externalTask.getId(), e);
                externalTaskService.handleFailure(externalTask, "Processing error", e.getMessage(), 0, 0L);
            }
        };
    }

    private int parseQuantity(Object quantityVar) {
        if (quantityVar instanceof Number) {
            return ((Number) quantityVar).intValue();
        } else if (quantityVar instanceof String) {
            String s = ((String) quantityVar).trim();
            if (s.isEmpty()) throw new NumberFormatException("empty string");
            return Integer.parseInt(s);
        } else if (quantityVar == null) {
            throw new NumberFormatException("quantity is null");
        } else {
            throw new NumberFormatException("unsupported quantity type: " + quantityVar.getClass());
        }
    }

    @PreDestroy
    void stop() {
        if (client != null) {
            try {
                client.stop();
                LOG.info("InventoryWorker client stopped");
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error stopping ExternalTaskClient", e);
            }
        }
    }
}