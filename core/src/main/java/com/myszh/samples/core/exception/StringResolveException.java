/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.core.exception;

/**
 * 字符串解析异常
 *
 * @author LuoQuan
 * @since 2022/6/12
 */
public class StringResolveException extends RuntimeException {
    private static final long serialVersionUID = 4529423530283546489L;

    public StringResolveException() {
    }

    public StringResolveException(String message) {
        super(message);
    }

    public StringResolveException(String message, Throwable cause) {
        super(message, cause);
    }

    public StringResolveException(Throwable cause) {
        super(cause);
    }
}
