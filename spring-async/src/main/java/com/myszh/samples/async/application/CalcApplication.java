/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.async.application;

import com.myszh.samples.async.service.CalcService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 计算
 *
 * @author LuoQuan
 * @since 2022/6/11
 */
@RestController
@RequestMapping("/calc")
public class CalcApplication {

    @Resource
    private CalcService calcService;

    /**
     * 计算等差数列前n项和(同步方式)
     *
     * @param start  开始
     * @param diff   差
     * @param number 前n项
     * @return 和
     */
    @GetMapping("/seq")
    public long calc(@RequestParam(name = "start", defaultValue = "1") int start,
                     @RequestParam(name = "diff", defaultValue = "1") int diff,
                     @RequestParam(name = "number", defaultValue = "1") int number) {
        return calcService.sumSequence(start, diff, number);
    }

    /**
     * 计算等差数列前n项和(同步方式)
     *
     * @param start  开始
     * @param diff   差
     * @param number 前n项
     * @return 和
     */
    @GetMapping("/async")
    public String calcAsync(@RequestParam(name = "start", defaultValue = "1") int start,
                            @RequestParam(name = "diff", defaultValue = "1") int diff,
                            @RequestParam(name = "number", defaultValue = "1") int number) {

        calcService.printSumSequenceAsync(start, diff, number);
        return "OK";
    }

}
