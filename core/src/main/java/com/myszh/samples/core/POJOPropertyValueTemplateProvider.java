/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.core;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * POJO属性值模板提供者
 *
 * @author LuoQuan
 * @since 2022/6/19
 */
public interface POJOPropertyValueTemplateProvider {

    /**
     * 获取POJO属性值的模板。
     *
     * @param parentPropertyName parent property name
     * @param propertyName       property name
     * @return 属性值的模板
     */
    default String getValueTemplate(String parentPropertyName, String propertyName) {
        return Optional.ofNullable(getValueTemplate(parentPropertyName))
            .map(properties -> properties.get(propertyName))
            .orElse(null);
    }

    /**
     * 如果可以通过POJO父节点名称获取全部子节点属性，实现这个方法即可
     * <p>
     * 实现类可以考虑在这里使用缓存以提高性能
     *
     * @param propertyName property name
     * @return Map
     */
    default Map<String, String> getValueTemplate(String propertyName) {
        return Collections.emptyMap();
    }

}
