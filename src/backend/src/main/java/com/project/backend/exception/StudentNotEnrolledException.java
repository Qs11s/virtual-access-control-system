package com.project.backend.exception;

public class StudentNotEnrolledException extends RuntimeException {
    public StudentNotEnrolledException() {
        super("未绑定该课程，无法签到");
    }

    public StudentNotEnrolledException(String message) {
        super(message);
    }
}
