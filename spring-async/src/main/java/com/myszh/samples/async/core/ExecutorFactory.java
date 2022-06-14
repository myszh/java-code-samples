/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.async.core;

import com.myszh.samples.async.core.ExecutorFactoryProperties.ExecutorDefinition;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.boot.task.TaskExecutorCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

/**
 * Executor工厂
 *
 * @author LuoQuan
 * @since 2022/6/14
 */
public class ExecutorFactory implements BeanDefinitionRegistryPostProcessor,
    EnvironmentAware, ApplicationContextAware, InitializingBean {

    private final Map<String, RejectedExecutionHandler> rejectedHandler = new HashMap<>(8);
    private ExecutorFactoryProperties executorFactoryProperties;
    private ApplicationContext applicationContext;
    @Getter
    private Supplier<Executor> primaryExecutorSupplier;

    public ExecutorFactory() {
        addRejectedHandler(new ThreadPoolExecutor.AbortPolicy());
        addRejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        addRejectedHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        addRejectedHandler(new ThreadPoolExecutor.DiscardPolicy());
    }

    private void addRejectedHandler(RejectedExecutionHandler rejectedHandler) {
        this.rejectedHandler.put(rejectedHandler.getClass().getSimpleName(), rejectedHandler);
    }

    @Override
    public void setEnvironment(Environment environment) {
        executorFactoryProperties = Binder.get(environment)
            .bind("application.task.executor", ExecutorFactoryProperties.class)
            .get();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        primaryExecutorSupplier = executorInstanceSupplier(
            executorFactoryProperties.getDefaultExecutor());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // do nothing
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        // 注册所有的Executor
        registerExecutorBeanDefinition(registry);
    }

    /**
     * 注册所有定义的Executor
     *
     * @param registry bean注册器
     */
    private void registerExecutorBeanDefinition(BeanDefinitionRegistry registry) {
        executorFactoryProperties.getExecutors()
            .forEach(properties -> {
                properties.setThreadNamePrefix(properties.getName() + "-");
                AbstractBeanDefinition executorsBeanDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(Executor.class, executorInstanceSupplier(properties))
                    .setPrimary(properties.isPrimary())
                    .setLazyInit(properties.isLazyInit())
                    .getBeanDefinition();

                registry.registerBeanDefinition(properties.getName(), executorsBeanDefinition);
            });
    }

    /**
     * 线程池实例提供者
     *
     * @param properties 线程池相关属性
     * @return Supplier
     */
    @SuppressWarnings({"unchecked"})
    public <E extends Executor> Supplier<E> executorInstanceSupplier(ExecutorDefinition properties) {
        ObjectProvider<TaskDecorator> taskDecorator =
            applicationContext.getBeanProvider(TaskDecorator.class);
        ObjectProvider<TaskExecutorCustomizer> taskExecutorCustomizers =
            applicationContext.getBeanProvider(TaskExecutorCustomizer.class);
        TaskExecutionProperties.Pool pool = properties.getPool();
        TaskExecutionProperties.Shutdown shutdown = properties.getShutdown();
        TaskExecutorBuilder builder = new TaskExecutorBuilder()
            .queueCapacity(pool.getQueueCapacity())
            .corePoolSize(pool.getCoreSize())
            .maxPoolSize(pool.getMaxSize())
            .allowCoreThreadTimeOut(pool.isAllowCoreThreadTimeout())
            .keepAlive(pool.getKeepAlive())
            .awaitTermination(shutdown.isAwaitTermination())
            .awaitTerminationPeriod(shutdown.getAwaitTerminationPeriod())
            .threadNamePrefix(properties.getThreadNamePrefix())
            // 可以通过 TaskExecutorCustomizer来定制线程池的一些属性
            .customizers(taskExecutorCustomizers.orderedStream()::iterator)
            .taskDecorator(taskDecorator.getIfUnique());
        return () -> {
            ThreadPoolTaskExecutor executor = builder.build();
            // 设置线程池的一些其他属性
            executor.setDaemon(properties.isDaemon());

            // 拒绝策略
            String rejectedExecutionHandler = properties.getRejectedExecutionHandler();
            RejectedExecutionHandler rejected;
            if (applicationContext.containsBean(rejectedExecutionHandler)) {
                rejected = applicationContext.getBean(rejectedExecutionHandler, RejectedExecutionHandler.class);
            } else {
                rejected = rejectedHandler.getOrDefault(rejectedExecutionHandler,
                    rejectedHandler.get(ThreadPoolExecutor.AbortPolicy.class.getSimpleName()));
            }
            executor.setRejectedExecutionHandler(rejected);
            return (E) executor;
        };
    }
}
