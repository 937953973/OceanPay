package com.oceanpay.enums;

import lombok.Getter;

/**
 * 登录类型枚举
 */
@Getter
public enum LoginType {
    PASSWORD(1, "密码登录"),
    VERIFICATION_CODE(2, "验证码登录"),
    THIRD_PARTY(3, "第三方登录");

    private final int code;
    private final String description;

    LoginType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static LoginType fromCode(int code) {
        for (LoginType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的登录类型代码: " + code);
    }
}