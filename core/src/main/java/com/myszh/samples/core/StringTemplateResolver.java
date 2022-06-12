/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.core;

import com.myszh.samples.core.exception.StringResolveException;
import org.springframework.asm.MethodVisitor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.TypedValue;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.CodeFlow;
import org.springframework.expression.spel.CompilablePropertyAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;
import org.springframework.util.Assert;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.util.StringUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 字符串模板解析器,基于Spring的core expression两个核心包
 * 1、支持占位符 ${name}
 * 2、支持SpEL(Spring Expression Language)如： #{person.name} #{1+2}
 *
 * @author LuoQuan
 * @see org.springframework.util.PropertyPlaceholderHelper
 * org.springframework.beans.factory.config.EmbeddedValueResolver
 * org.springframework.context.expression.StandardBeanExpressionResolver
 * @since 2022/6/11
 */
public class StringTemplateResolver {
    /**
     * 单实例
     */
    private final static StringTemplateResolver sharedInstance = new StringTemplateResolver();
    /**
     * 严格模式解析器
     */
    private final PropertyPlaceholderHelper strictHelper = createHelper(false);

    /**
     * 非严格模式解析器
     */
    private final PropertyPlaceholderHelper nonStrictHelper = createHelper(true);

    /**
     * 表达式缓存
     */
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>(128);

    /**
     * 缓存所有的bean属性
     */
    private final Map<Class<?>, Map<String, PropertyReadable>> cacheProperty = new ConcurrentHashMap<>();

    /**
     * Map Accessor
     */
    private final MapAccessor mapAccessor = new MapAccessor();

    /**
     * 私有化构造器，单实例对象
     */
    private StringTemplateResolver() {
    }

    /**
     * 获取实例对象
     *
     * @return StringTemplateResolver
     */
    public static StringTemplateResolver getInstance() {
        return sharedInstance;
    }

    /**
     * 解析字符串模板(非严格模式)
     *
     * @param template 模板
     * @param context  上下文
     * @return String
     */
    public String parse(String template, Object context) {
        return parse(template, context, false);
    }

    /**
     * 解析字符串模板,多上下文(非严格模式)
     *
     * @param template     模板
     * @param multiContext 多上下文
     * @return String
     */
    public String parse(String template, MultiContext multiContext) {
        return parse(template, multiContext, false);
    }

    /**
     * 解析字符串模板,多上下文
     *
     * @param template     模板
     * @param multiContext 多上下文
     * @param isStrict     是否严格模式
     * @return String
     */
    public String parse(String template, MultiContext multiContext, boolean isStrict) {
        Map<String, Object> context = Optional.ofNullable(multiContext)
            .map(MultiContext::getMultiContext)
            .orElse(null);
        return parse(template, context, isStrict);
    }

    /**
     * 解析字符串模板
     *
     * @param template 模板
     * @param context  上下文
     * @param isStrict 是否严格模式
     * @return String
     */
    public String parse(String template, Object context, boolean isStrict) {
        // 先解析占位符
        String value = replacePlaceholders(template, context, isStrict);
        // 解析表达式
        return evaluate(value, context);
    }

    /**
     * 创建占位符解析器
     *
     * @param ignoreUnresolvablePlaceholders 是否忽略无法解析的占位符
     * @return PropertyPlaceholderHelper
     */
    private PropertyPlaceholderHelper createHelper(boolean ignoreUnresolvablePlaceholders) {
        return new PropertyPlaceholderHelper(
            "${", "}", ":", ignoreUnresolvablePlaceholders);
    }

    /**
     * 解析表达式
     *
     * @param value   表达式
     * @param context 上下文
     * @return String
     */
    private String evaluate(String value, Object context) {
        if (!StringUtils.hasLength(value)) {
            return value;
        }
        StandardEvaluationContext exprContext = new StandardEvaluationContext(context);
        exprContext.setTypeConverter(new StandardTypeConverter(DefaultConversionService::getSharedInstance));
        exprContext.addPropertyAccessor(mapAccessor);

        return Optional.of(getExpression(value))
            .map(expr -> expr.getValue(exprContext))
            .map(Object::toString)
            .orElse(null);
    }

    /**
     * 获取表达式
     *
     * @param value 表达式字符串
     * @return Expression
     */
    private Expression getExpression(String value) {
        Expression expr = expressionCache.get(value);
        if (expr == null) {
            expr = new SpelExpressionParser().parseExpression(value, new TemplateParserContext());
            expressionCache.put(value, expr);
        }
        return expr;
    }

    /**
     * 解析占位符
     *
     * @param template 模板
     * @param context  上下文
     * @param isStrict 是否严格模式
     * @return String
     */
    private String replacePlaceholders(String template, Object context, boolean isStrict) {
        PlaceholderResolver placeholderResolver = builderPlaceholderResolver(context);
        if (isStrict) {
            return strictHelper.replacePlaceholders(template, placeholderResolver);
        } else {
            return nonStrictHelper.replacePlaceholders(template, placeholderResolver);
        }
    }

    /**
     * 构建占位符解析器
     *
     * @param context context
     * @return PlaceholderResolver
     */
    private PlaceholderResolver builderPlaceholderResolver(Object context) {
        if (context instanceof Map) {
            Map<?, ?> placeholderValueMap = (Map<?, ?>) context;
            return key -> Optional.ofNullable(placeholderValueMap.get(key))
                .map(Object::toString)
                .orElse(null);
        }
        Map<String, PropertyReadable> properties = getReadableProperties(context);
        return name -> Optional.ofNullable(properties.get(name))
            .map(prop -> prop.readString(context))
            .orElse(null);
    }

    /**
     * 获取bean可读属性
     *
     * @param bean bean
     * @return Map
     */
    private Map<String, PropertyReadable> getReadableProperties(Object bean) {
        if (bean == null) {
            return Collections.emptyMap();
        }
        Class<?> beanClass = bean.getClass();

        Map<String, PropertyReadable> propertyMap = cacheProperty.get(beanClass);
        if (propertyMap != null) {
            return propertyMap;
        }

        try {
            propertyMap = Optional.ofNullable(Introspector.getBeanInfo(beanClass))
                .map(BeanInfo::getPropertyDescriptors)
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .map(PropertyReadable::new)
                .filter(PropertyReadable::isReadable)
                .collect(Collectors.toMap(PropertyReadable::getName, property -> property));
            cacheProperty.put(beanClass, propertyMap);
        } catch (IntrospectionException e) {
            throw new StringResolveException("Resolve bean property failed", e);
        }
        return propertyMap;
    }

    /**
     * 可读的属性
     */
    static class PropertyReadable {
        /**
         * 属性名称
         */
        private final PropertyDescriptor propertyDescriptor;
        /**
         * 属性读方法
         */
        private final Method readMethod;

        PropertyReadable(PropertyDescriptor propertyDescriptor) {
            this.propertyDescriptor = propertyDescriptor;
            readMethod = propertyDescriptor.getReadMethod();
        }

        /**
         * 读取字符串
         *
         * @param instance 实例对象
         * @return String
         */
        String readString(Object instance) {
            if (readMethod == null) {
                return null;
            }
            try {
                return Optional.ofNullable(readMethod.invoke(instance))
                    .map(Object::toString)
                    .orElse(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new StringResolveException(
                    "Read bean[" + instance.getClass() + "] property[" + getName() + "] failed", e);
            }
        }

        /**
         * 是否可读
         *
         * @return true, false
         */
        private boolean isReadable() {
            return readMethod != null;
        }

        /**
         * 获取属性名称
         *
         * @return String
         */
        private String getName() {
            return propertyDescriptor.getName();
        }
    }

    /**
     * Map Accessor:读取context是map的
     */
    private static class MapAccessor implements CompilablePropertyAccessor {
        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return new Class<?>[]{Map.class};
        }

        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) {
            return (target instanceof Map && ((Map<?, ?>) target).containsKey(name));
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) {
            Assert.state(target instanceof Map, "Target must be of type Map");
            Map<?, ?> map = (Map<?, ?>) target;
            Object value = map.get(name);
            if (value == null && !map.containsKey(name)) {
                throw new StringResolveException("Map does not contain a value for key '" + name + "'");
            }
            if (value instanceof Supplier) {
                value = ((Supplier<?>) value).get();
            }
            return new TypedValue(value);
        }

        @Override
        public boolean canWrite(EvaluationContext context,
                                Object target, String name) {
            return true;
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public void write(EvaluationContext context, Object target,
                          String name, Object newValue) {
            Assert.state(target instanceof Map, "Target must be a Map");
            Map<Object, Object> map = (Map<Object, Object>) target;
            map.put(name, newValue);
        }

        @Override
        public boolean isCompilable() {
            return true;
        }

        @Override
        public Class<?> getPropertyType() {
            return Object.class;
        }

        @Override
        public void generateCode(String propertyName, MethodVisitor mv, CodeFlow cf) {
            String descriptor = cf.lastDescriptor();
            if (descriptor == null || !descriptor.equals("Ljava/util/Map")) {
                if (descriptor == null) {
                    cf.loadTarget(mv);
                }
                CodeFlow.insertCheckCast(mv, "Ljava/util/Map");
            }
            mv.visitLdcInsn(propertyName);
            mv.visitMethodInsn(
                INVOKEINTERFACE, "java/util/Map", "get",
                "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        }
    }
}
