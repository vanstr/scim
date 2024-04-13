package com.scim.impl.service;

import lombok.Getter;

@Getter
public class ScimException extends RuntimeException {
    private final int errorCode;
    public ScimException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ScimException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
