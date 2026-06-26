package com.arcsoft.arcfacedemo.faceserver;

/**
 * 人脸注册失败时抛出的业务异常，携带失败原因描述。
 */
public class RegisterFailedException extends Exception {
    /** @param message 注册失败原因 */
    public RegisterFailedException(String message) {
        super(message);
    }
}
