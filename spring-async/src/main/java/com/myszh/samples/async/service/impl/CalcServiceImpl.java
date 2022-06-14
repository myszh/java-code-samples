/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.async.service.impl;

import com.myszh.samples.async.core.UserContext;
import com.myszh.samples.async.service.CalcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 计算服务实现
 *
 * @author LuoQuan
 * @since 2022/6/11
 */
@Service
@Slf4j
public class CalcServiceImpl implements CalcService, ApplicationContextAware {

    /**
     * 注入自己(代理对象)，这里不能直接使用注解的方式来注入，
     * 需要在初始化方法中注入(晚于BeanPostProcessor#postProcessAfterInitialization方法的执行)
     */
    private final ObjectProvider<CalcServiceImpl> selfProvider;

    private CalcServiceImpl self;

    public CalcServiceImpl(ObjectProvider<CalcServiceImpl> selfProvider) {
        this.selfProvider = selfProvider;
    }

    /**
     * 这里不能通过self属性上加注解来注入自己，因为@Async的包装方式不是提前暴露的
     * {@link org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor#getEarlyBeanReference},
     * 而是使用{@link org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization}来包装代理对象的。
     * 所以我们只能在初始化方法中来获取自己的引用（Spring在保证单实例唯一性做的校验）
     *
     * @see org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor
     */
    @PostConstruct
    public void init() {
        self = selfProvider.getIfUnique();
    }

    /**
     * 保存数据（异步）
     *
     * @param data data
     */
    @Async
    public void saveDataAsync(long data) {
        log.info("save data data={}", data);
        log.info("user name{}", UserContext.getUser().getName());
    }

    @Override
    public long sumSequence(long start, long diff, long number) {
        // 通过本类的代理对象调用自己的方法
        self.saveDataAsync(start);
        log.info("user name{}", UserContext.getUser().getName());
        // 阻塞5s, 模拟执行过程中耗时较长
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
        // Sn=n*a1+n(n-1)d/2
        return number * start + ((number - 1) * number * diff) / 2;
    }

    @Override
    public void printSumSequenceAsync(long start, long diff, long number) {
        long sum = sumSequence(start, diff, number);
        log.info("等差数列和为:{}", sum);
        log.info("user name{}", UserContext.getUser().getName());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ObjectProvider<CalcServiceImpl> beanProvider = applicationContext.getBeanProvider(CalcServiceImpl.class);

    }
}
