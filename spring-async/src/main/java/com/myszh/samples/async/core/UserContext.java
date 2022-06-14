/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.async.core;

/**
 * User
 *
 * @author LuoQuan
 * @since 2022/6/12
 */
public abstract class UserContext {
    private final static ThreadLocal<User> userTL = new ThreadLocal<>();

    public static User getUser() {
        return userTL.get();
    }

    public static void setUser(User user) {
        userTL.set(user);
    }
}
