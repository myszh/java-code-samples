/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.core.exception;

/**
 * 构建异常
 *
 * @author LuoQuan
 * @since 2022/6/19
 */
public class BuildException extends RuntimeException {

    private static final long serialVersionUID = -8892099476904946184L;

    public BuildException() {
    }

    public BuildException(String message) {
        super(message);
    }

    public BuildException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuildException(Throwable cause) {
        super(cause);
    }

    public BuildException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
