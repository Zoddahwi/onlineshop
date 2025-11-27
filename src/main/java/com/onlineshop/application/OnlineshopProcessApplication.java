package com.onlineshop.application;

import jakarta.enterprise.context.ApplicationScoped;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;

/**
 * Process application entry point that exposes the container BeanManager to Camunda when
 * deploying the process archive (WAR). Necessary so expressions like ${delegateLookupAdapter}
 * can be resolved against CDI beans.
 */
@ProcessApplication("onlineshop-process-application")
@ApplicationScoped
public class OnlineshopProcessApplication extends  ServletProcessApplication {
    // no additional code required
}