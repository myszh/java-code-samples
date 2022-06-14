/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.async.config;

import com.myszh.samples.async.core.ExecutorFactory;
import com.myszh.samples.async.core.User;
import com.myszh.samples.async.core.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncExecutionInterceptor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.*;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * 使用Spring @Async注解开发异步方法：最快捷的方式
 * 1、启用Async功能：在Spring组件中加上@EnableAsync注解，告诉Spring启用Async
 * 2、在需要异步的方法上加上@Async注解，这个方法就是一个异步执行的方法了
 *
 * @author LuoQuan
 * @see ProxyAsyncConfiguration
 * @see AsyncAnnotationBeanPostProcessor
 * @see AsyncAnnotationAdvisor
 * @see AsyncExecutionInterceptor#invoke
 * @see TaskExecutionAutoConfiguration
 * @since 2022/6/11
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfiguration {

    /**
     * 异步未处理的异常
     *
     * @param exception 异常
     * @param method    method
     * @param params    params
     */
    private void asyncUncaughtException(Throwable exception, Method method, Object... params) {
        if (log.isErrorEnabled()) {
            ReflectionUtils.invokeMethod(method, new Object());
            log.error("Unexpected exception occurred invoking async method:{}", method, exception);
        }

        if (Future.class.isAssignableFrom(method.getReturnType())) {
            ReflectionUtils.rethrowRuntimeException(exception);
        }
    }

    /**
     * 提供AsyncConfigurer
     *
     * @param executorProvider executor provider
     * @return AsyncConfigurer
     */
    @Bean
    public AsyncConfigurer createAsyncConfigurer(ObjectProvider<Executor> executorProvider) {
        return new AsyncConfigurer() {
            @Override
            public Executor getAsyncExecutor() {
                return executorProvider.getIfUnique();
            }

            /**
             * 替换Spring默认的异步执行异常处理方式
             * @see  org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler
             * @return AsyncUncaughtExceptionHandler
             */
            @Override
            public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
                return (exception, method, params) -> asyncUncaughtException(exception, method, params);
            }
        };
    }

    /**
     * 线程之间传递数据(ThreadLocal)，使用TaskDecorator
     *
     * @return TaskDecorator
     * @see ThreadLocal
     */
    @Bean
    public TaskDecorator createTaskDecorator() {
        return runnable -> {
            // 当前线程，获取User
            User user = UserContext.getUser();
            return () -> {
                // 下一个线程开始执行之前，设置User
                UserContext.setUser(user);
                runnable.run();
            };
        };
    }

    /**
     * 创建Executor Factory，可以通过声明式创建Executor
     *
     * @return ExecutorFactory
     * @see com.myszh.samples.async.core.ExecutorFactoryProperties
     */
    @Bean
    public ExecutorFactory createExecutorFactory() {
        return new ExecutorFactory();
    }

    /**
     * 这里暴露一个 PrimaryExecutor是为了让 {@link TaskExecutionAutoConfiguration#applicationTaskExecutor}
     * 配置的Executor不起作用。原因是{@link ExecutorFactory}里面注入的Executor是在 ConditionalOnMissingBean之后的
     *
     * @param executorFactory executorFactory
     * @return Executor
     */
    @Lazy
    @Primary
    @Bean
    public Executor primaryExecutor(ExecutorFactory executorFactory) {
        return executorFactory.getPrimaryExecutorSupplier().get();
    }
}
