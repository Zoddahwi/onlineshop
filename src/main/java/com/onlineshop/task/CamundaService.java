package com.onlineshop.task;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class CamundaService {
    private static final Logger LOG = Logger.getLogger(CamundaService.class.getName());

    /**
     * Start a process instance by key with provided variables.
     * Currently a stub that logs the request; replace with Camunda REST/engine integration as needed.
     */
    public void startProcessInstanceByKey(String processKey, Map<String, Object> variables) {
        LOG.log(Level.INFO, "startProcessInstanceByKey: key={0}, variables={1}", new Object[]{processKey, variables});
    }

    /**
     * Correlate a message to a process instance (stub).
     */
    public void correlateMessage(String messageName, String businessKey, Map<String, Object> variables) {
        LOG.log(Level.INFO, "correlateMessage: message={0}, businessKey={1}, variables={2}",
                new Object[]{messageName, businessKey, variables});
    }

    /**
     * Complete a task (stub).
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        LOG.log(Level.INFO, "completeTask: taskId={0}, variables={1}", new Object[]{taskId, variables});
    }

}
