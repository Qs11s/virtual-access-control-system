package com.project.backend.exception;

public class ExpiredSessionException extends RuntimeException {
    public ExpiredSessionException() {
        super("场次已过期或尚未开始，无法签到");
    }

    public ExpiredSessionException(String message) {
        super(message);
    }
}
