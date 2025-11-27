package com.onlineshop.config;

import com.onlineshop.task.CheckProductDelegate;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class CamundaBeanRegistrar {

    @Inject
    Instance<ProcessEngine> processEngineInstance;

    @Inject
    Instance<CheckProductDelegate> checkProductInstance;

    void onStart(@Observes StartupEvent ev) {
        Thread t = new Thread(() -> {
            final int maxAttempts = 30;
            final long sleepMs = 500L;
            int attempt = 0;
            while (attempt < maxAttempts) {
                attempt++;
                try {
                    ProcessEngine processEngine = null;

                    if (processEngineInstance != null && !processEngineInstance.isUnsatisfied()) {
                        try {
                            processEngine = processEngineInstance.get();
                        } catch (Exception ignored) {}
                    }

                    if (processEngine == null) {
                        try { processEngine = ProcessEngines.getDefaultProcessEngine(); } catch (Exception ignored) {}
                    }

                    if (processEngine == null) {
                        Thread.sleep(sleepMs);
                        continue;
                    }

                    ProcessEngineConfigurationImpl cfg =
                            (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

                    if (cfg == null) {
                        Thread.sleep(sleepMs);
                        continue;
                    }

                    Map<Object, Object> beans = cfg.getBeans();
                    if (beans == null) beans = new HashMap<>();

                    // use CDI-managed instance instead of new
                    if (checkProductInstance != null && !checkProductInstance.isUnsatisfied()) {
                        beans.put("checkProductDelegate", checkProductInstance.get());
                    }

                    cfg.setBeans(beans);
                    return; // success
                } catch (Exception ex) {
                    try { Thread.sleep(sleepMs); } catch (InterruptedException ignored) {}
                }
            }
        }, "camunda-bean-registrar");
        t.setDaemon(true);
        t.start();
    }
}