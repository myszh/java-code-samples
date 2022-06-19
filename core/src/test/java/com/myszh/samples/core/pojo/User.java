/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.core.pojo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * user
 *
 * @author LuoQuan
 * @since 2022/6/19
 */
@Data
public class User {

    private String name;

    private BigDecimal age;

    private User friend;

    private Animal pet;

    private boolean girl;

    public boolean getGirl() {
        return girl;
    }
}
