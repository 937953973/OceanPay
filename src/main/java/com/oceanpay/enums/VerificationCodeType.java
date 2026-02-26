package com.oceanpay.enums;

import lombok.Getter;

/**
 * 验证码类型枚举
 */
@Getter
public enum VerificationCodeType {
    REGISTER(1, "注册"),
    LOGIN(2, "登录"),
    RESET_PASSWORD(3, "重置密码"),
    BIND_EMAIL(4, "绑定邮箱"),
    BIND_PHONE(5, "绑定手机");

    private final int code;
    private final String description;

    VerificationCodeType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static VerificationCodeType fromCode(int code) {
        for (VerificationCodeType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的验证码类型代码: " + code);
    }
}