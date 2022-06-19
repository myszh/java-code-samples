/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.core;

import java.util.Map;
import java.util.function.Predicate;

/**
 * POJO（Plain Ordinary Java Object）简单的Java对象构建者
 *
 * @author LuoQuan
 * @since 2022/6/19
 */
@FunctionalInterface
public interface POJOBuilder {

    /**
     * 构建一个POJO实例,并且可通过Predicate来控制是否构建
     *
     * @param type           POJO类型
     * @param buildContext   构建过程中的上下文
     * @param definitionName 定义如何构建POJO实例的模板名称
     * @param beforeBuild    构建之前的回调
     * @param <T>            POJO泛型参数
     * @return T
     */
    <T> T build(Class<T> type, Object buildContext, String definitionName,
        Predicate<Map<String, Object>> beforeBuild);

    /**
     * @see #build(Class, Object, String, Predicate)
     */
    default <T> T build(Class<T> type, Object buildContext, String definitionName) {
        return build(type, buildContext, definitionName, null);
    }
}
