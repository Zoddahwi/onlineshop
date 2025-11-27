package com.onlineshop.task;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.logging.Logger;

@ApplicationScoped
public class DelegateLookupAdapter implements JavaDelegate {

    private static final Logger LOG = Logger.getLogger(DelegateLookupAdapter.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Instance<CheckProductDelegate> instance = CDI.current().select(CheckProductDelegate.class);

        if (instance.isUnsatisfied()) {
            LOG.severe("CheckProductDelegate CDI bean not available!");
            throw new IllegalStateException("CheckProductDelegate CDI bean not available");
        }

        CheckProductDelegate delegate = instance.get();

        LOG.info("DelegateLookupAdapter: executing CheckProductDelegate...");
        delegate.execute(execution);
        LOG.info("CheckProductDelegate executed successfully.");
    }
}
