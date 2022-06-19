/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.core;

import com.alibaba.fastjson.JSONObject;
import com.myszh.samples.core.exception.BuildException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

/**
 * POJOBuilder默认实现
 *
 * @author LuoQuan
 * @since 2022/6/19
 */
public class DefaultPOJOBuilder implements POJOBuilder {

    private final StringTemplateResolver templateResolver;

    private final POJOPropertyValueTemplateProvider valueTemplate;

    public DefaultPOJOBuilder(POJOPropertyValueTemplateProvider valueTemplate,
        StringTemplateResolver templateResolver) {
        Objects.requireNonNull(valueTemplate);
        this.valueTemplate = valueTemplate;
        this.templateResolver = templateResolver;
    }

    public DefaultPOJOBuilder(Properties properties) {
        this(new POJOPropertyValueTemplateProvider() {
                 @Override
                 public String getValueTemplate(String parentPropertyName, String propertyName) {
                     return properties.getProperty(parentPropertyName + "." + propertyName);
                 }
             },
            StringTemplateResolver.getInstance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T build(Class<T> type, Object buildContext, String definitionName,
        Predicate<Map<String, Object>> beforeBuild) {
        try {
            if (Objects.nonNull(beforeBuild) && !beforeBuild.test(getConfig(definitionName))) {
                return null;
            }

            return new Binder(name -> getProperty(name, buildContext))
                .bind(definitionName, type)
                .get();
        } catch (Throwable throwable) {
            throw new BuildException("build POJO[" + type.getName() + "] error!!", throwable);
        }
    }

    private Map<String, Object> getConfig(String definitionName) {
        String config = valueTemplate.getValueTemplate(
            definitionName, "__config__");
        return JSONObject.parseObject(config);
    }


    private ConfigurationProperty getProperty(ConfigurationPropertyName name, Object buildContext) {
        String valueTemplate = this.valueTemplate.getValueTemplate(
            name.getParent().toString(),
            name.toString());
        // 解析
        String value = templateResolver.parse(valueTemplate, buildContext);
        return new ConfigurationProperty(name, value, null);
    }
}
