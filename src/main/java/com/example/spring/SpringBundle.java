package com.example.spring;

import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Preconditions;

import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class SpringBundle<T extends Configuration> implements ConfiguredBundle<T> {
    private static final Logger LOG = LoggerFactory.getLogger(SpringBundle.class);
    public static final String CONFIGURATION_BEAN_NAME = "dw";
    public static final String ENVIRONMENT_BEAN_NAME = "dwEnv";
    private ConfigurableApplicationContext context;
    private boolean registerConfiguration;
    private boolean registerEnvironment;

    public SpringBundle(ConfigurableApplicationContext context) {
        this(context, false, false);
    }

    public SpringBundle(ConfigurableApplicationContext context, boolean registerConfiguration, boolean registerEnvironment) {
        if (registerConfiguration || registerEnvironment) {
            Preconditions.checkArgument(!context.isActive(), "Context must be not active in order to register configuration, environment or placeholder");
        }

        this.context = context;
        this.registerConfiguration = registerConfiguration;
        this.registerEnvironment = registerEnvironment;
    }

    public void run(T configuration, Environment environment) throws Exception {
        if (this.registerConfiguration) {
            this.registerConfiguration(configuration, this.context);
        }

        if (this.registerEnvironment) {
            this.registerEnvironment(environment, this.context);
        }

        if (!this.context.isActive()) {
            this.context.refresh();
        }

        this.registerManaged(environment, this.context);
        this.registerLifecycle(environment, this.context);
        this.registerServerLifecycleListeners(environment, this.context);
        this.registerTasks(environment, this.context);
        this.registerHealthChecks(environment, this.context);
        this.registerInjectableProviders(environment, this.context);
        this.registerProviders(environment, this.context);
        this.registerResources(environment, this.context);
    }

    public void initialize(Bootstrap<?> bootstrap) {
    }

    public ConfigurableApplicationContext getContext() {
        return this.context;
    }

    public void setRegisterConfiguration(boolean registerConfiguration) {
        this.registerConfiguration = registerConfiguration;
    }

    public void setRegisterEnvironment(boolean registerEnvironment) {
        this.registerEnvironment = registerEnvironment;
    }

    private void registerManaged(Environment environment, ConfigurableApplicationContext context) {
        Map<String, Managed> beansOfType = context.getBeansOfType(Managed.class);
        Iterator var4 = beansOfType.keySet().iterator();

        while(var4.hasNext()) {
            String beanName = (String)var4.next();
            Managed managed = (Managed)beansOfType.get(beanName);
            environment.lifecycle().manage(managed);
            LOG.info("Registering managed: " + managed.getClass().getName());
        }

    }

    private void registerLifecycle(Environment environment, ConfigurableApplicationContext context) {
        Map<String, LifeCycle> beansOfType = context.getBeansOfType(LifeCycle.class);
        Iterator var4 = beansOfType.keySet().iterator();

        while(var4.hasNext()) {
            String beanName = (String)var4.next();
            if (!beanName.equals("dwEnv")) {
                LifeCycle lifeCycle = (LifeCycle)beansOfType.get(beanName);
                environment.lifecycle().manage(lifeCycle);
                LOG.info("Registering lifeCycle: " + lifeCycle.getClass().getName());
            }
        }

    }

    private void registerServerLifecycleListeners(Environment environment, ConfigurableApplicationContext context) {
        Map<String, ServerLifecycleListener> beansOfType = context.getBeansOfType(ServerLifecycleListener.class);
        Iterator var4 = beansOfType.keySet().iterator();

        while(var4.hasNext()) {
            String beanName = (String)var4.next();
            if (!beanName.equals("dwEnv")) {
                ServerLifecycleListener serverLifecycleListener = (ServerLifecycleListener)beansOfType.get(beanName);
                environment.lifecycle().addServerLifecycleListener(serverLifecycleListener);
                LOG.info("Registering serverLifecycleListener: " + serverLifecycleListener.getClass().getName());
            }
        }

    }

    private void registerTasks(Environment environment, ConfigurableApplicationContext context) {
        Map<String, Task> beansOfType = context.getBeansOfType(Task.class);
        Iterator var4 = beansOfType.keySet().iterator();

        while(var4.hasNext()) {
            String beanName = (String)var4.next();
            Task task = (Task)beansOfType.get(beanName);
            environment.admin().addTask(task);
            LOG.info("Registering task: " + task.getClass().getName());
        }

    }

    private void registerHealthChecks(Environment environment, ConfigurableApplicationContext context) {
        Map<String, HealthCheck> beansOfType = context.getBeansOfType(HealthCheck.class);
        Iterator var4 = beansOfType.keySet().iterator();

        while(var4.hasNext()) {
            String beanName = (String)var4.next();
            HealthCheck healthCheck = (HealthCheck)beansOfType.get(beanName);
            environment.healthChecks().register(beanName, healthCheck);
            LOG.info("Registering healthCheck: " + healthCheck.getClass().getName());
        }

    }

    private void registerInjectableProviders(Environment environment, ConfigurableApplicationContext context) {
        Map<String, InjectionResolver> beansOfType = context.getBeansOfType(InjectionResolver.class);
        Iterator var4 = beansOfType.keySet().iterator();

        while(var4.hasNext()) {
            String beanName = (String)var4.next();
            InjectionResolver injectableProvider = (InjectionResolver)beansOfType.get(beanName);
            environment.jersey().register(injectableProvider);
            LOG.info("Registering injectable provider: " + injectableProvider.getClass().getName());
        }

    }

    private void registerProviders(Environment environment, ConfigurableApplicationContext context) {
        Map<String, Object> beansWithAnnotation = context.getBeansWithAnnotation(Provider.class);
        Iterator var4 = beansWithAnnotation.keySet().iterator();

        while(var4.hasNext()) {
            String beanName = (String)var4.next();
            Object provider = beansWithAnnotation.get(beanName);
            environment.jersey().register(provider);
            LOG.info("Registering provider : " + provider.getClass().getName());
        }

    }

    private void registerResources(Environment environment, ConfigurableApplicationContext context) {
        Map<String, Object> beansWithAnnotation = context.getBeansWithAnnotation(Path.class);
        Iterator var4 = beansWithAnnotation.keySet().iterator();

        while(var4.hasNext()) {
            String beanName = (String)var4.next();
            Object resource = beansWithAnnotation.get(beanName);
            environment.jersey().register(resource);
            LOG.info("Registering resource : " + resource.getClass().getName());
        }

    }

    private void registerConfiguration(T configuration, ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        beanFactory.registerSingleton("dw", configuration);
        LOG.info("Registering Dropwizard Configuration under name : dw");
    }

    private void registerEnvironment(Environment environment, ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        beanFactory.registerSingleton("dwEnv", environment);
        LOG.info("Registering Dropwizard Environment under name : dwEnv");
    }
}