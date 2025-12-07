package com.project.backend.exception;

public class DuplicateCheckInException extends RuntimeException {
    public DuplicateCheckInException() {
        super("已完成签到，禁止重复操作");
    }

    public DuplicateCheckInException(String message) {
        super(message);
    }
}
