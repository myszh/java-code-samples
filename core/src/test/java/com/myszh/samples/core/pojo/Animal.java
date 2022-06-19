/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.core.pojo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 动物
 *
 * @author LuoQuan
 * @since 2022/6/19
 */
@Data
public class Animal {

    private String name;

    private String subjects;

    private BigDecimal weight;
}
