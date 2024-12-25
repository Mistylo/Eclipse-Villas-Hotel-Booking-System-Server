package com.daliycode.HotelBookingSystem.exception;

public class UserNotFoundException extends RuntimeException {

    // 构造函数
    public UserNotFoundException(String message) {
        super(message);
    }
}
