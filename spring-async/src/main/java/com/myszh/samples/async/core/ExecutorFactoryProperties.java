/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.async.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Executor配置
 *
 * @author LuoQuan
 * @since 2022/6/14
 */
@Data
public class ExecutorFactoryProperties {

    private final ExecutorDefinition defaultExecutor = new ExecutorDefinition();

    private final List<ExecutorBeanDefinition> executors = new ArrayList<>();

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ExecutorDefinition extends TaskExecutionProperties {
        /**
         * 守护线程
         */
        private boolean daemon = false;

        /**
         * 拒绝策略
         */
        private String rejectedExecutionHandler = ThreadPoolExecutor.AbortPolicy.class.getSimpleName();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ExecutorBeanDefinition extends ExecutorDefinition {
        /**
         * bean name
         */
        private String name;

        private boolean lazyInit = true;

        private boolean primary = false;
    }
}
