package com.onlineshop.config;

import com.onlineshop.task.CheckProductDelegate;
import com.onlineshop.task.PlaceOrderDelegate;
import com.onlineshop.task.NotifySuccessDelegate;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.arc.Arc;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CamundaEngineBeansPlugin extends AbstractProcessEnginePlugin {

    private static final Logger LOG = Logger.getLogger(CamundaEngineBeansPlugin.class.getName());

    @Override
    public void preInit(ProcessEngineConfigurationImpl configuration) {
        LOG.info("CamundaEngineBeansPlugin.preInit() - registering CDI delegates via Arc");

        Map<Object, Object> beans = configuration.getBeans();
        if (beans == null) {
            beans = new HashMap<>();
        }

        // lookup CDI beans via Arc (Quarkus)
        InstanceHandle<CheckProductDelegate> checkHandle = Arc.container().instance(CheckProductDelegate.class);
        if (checkHandle.isAvailable()) {
            beans.put("checkProductDelegate", checkHandle.get());
        } else {
            LOG.warning("CheckProductDelegate not available in CDI");
        }

        InstanceHandle<PlaceOrderDelegate> placeHandle = Arc.container().instance(PlaceOrderDelegate.class);
        if (placeHandle.isAvailable()) {
            beans.put("placeOrderDelegate", placeHandle.get());
        } else {
            LOG.warning("PlaceOrderDelegate not available in CDI");
        }

        InstanceHandle<NotifySuccessDelegate> notifyHandle = Arc.container().instance(NotifySuccessDelegate.class);
        if (notifyHandle.isAvailable()) {
            beans.put("notifySuccessDelegate", notifyHandle.get());
        } else {
            LOG.warning("NotifySuccessDelegate not available in CDI");
        }

        configuration.setBeans(beans);
    }
}