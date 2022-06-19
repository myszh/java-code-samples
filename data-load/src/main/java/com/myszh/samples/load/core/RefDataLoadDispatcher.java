/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.load.core;

import java.util.Collections;

/**
 * 数据加载调度器，将数据加载分配到具体的加载器
 *
 * @author LuoQuan
 * @since 2022/6/20
 */
public interface RefDataLoadDispatcher {

    /**
     * @see #executeLoad(Iterable)
     */
    default <T> void executeLoad(T obj) {
        executeLoad(Collections.singletonList(obj));
    }

    /**
     * 加载一批数据
     *
     * @param iterable iterable
     * @param <T>      T
     */
    <T> void executeLoad(Iterable<T> iterable);
}
