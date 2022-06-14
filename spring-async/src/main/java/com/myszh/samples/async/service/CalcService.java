/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.async.service;

import org.springframework.scheduling.annotation.Async;

/**
 * 计算服务
 *
 * @author LuoQuan
 * @since 2022/6/11
 */
public interface CalcService {

    /**
     * 计算等差数列和
     *
     * @param start  开始值
     * @param diff   差
     * @param number 数量
     * @return 结果
     */
    long sumSequence(long start, long diff, long number);

    /**
     * 计算等差数列和，然后打印
     *
     * @param start  开始值
     * @param diff   差
     * @param number 数量
     */
    @Async
    void printSumSequenceAsync(long start, long diff, long number);
}
