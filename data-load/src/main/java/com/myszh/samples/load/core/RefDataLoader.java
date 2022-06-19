/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.load.core;

import java.util.List;

/**
 * 引用数据类型加载器
 *
 * @author LuoQuan
 * @since 2022/6/20
 */
@FunctionalInterface
public interface RefDataLoader<T extends RefData> {

    /**
     * 更新引用数据类型属性
     *
     * @param refDataList 引用数据类型列表
     */
    void updateRefDataProperty(List<T> refDataList);
}
