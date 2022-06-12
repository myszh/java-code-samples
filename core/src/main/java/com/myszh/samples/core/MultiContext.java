/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 多上下文
 *
 * @author LuoQuan
 * @since 2022/6/12
 */
public class MultiContext {
    /**
     * 内部多上下文map
     */
    private final Map<String, Object> multiContext = new HashMap<>();

    /**
     * 静态方法构造
     *
     * @return MultiContext
     */
    public static MultiContext of() {
        return new MultiContext();
    }

    /**
     * 获取内部多上下文map
     *
     * @return map
     */
    Map<String, Object> getMultiContext() {
        return multiContext;
    }

    /**
     * 添加上下文
     *
     * @param name    名称
     * @param context 上下文
     * @return MultiContext
     */
    public MultiContext add(String name, Object context) {
        multiContext.put(name, context);
        return this;
    }

    /**
     * 添加上下文提供者，使用这种方式可以达到懒加载的效果
     *
     * @param name            名称
     * @param contextProvider 上下文提供者
     * @return MultiContext
     */
    public MultiContext add(String name, Supplier<?> contextProvider) {
        multiContext.put(name, contextProvider);
        return this;
    }

}
