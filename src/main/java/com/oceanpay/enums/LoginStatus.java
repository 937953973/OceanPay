package com.oceanpay.enums;

import lombok.Getter;

/**
 * 登录状态枚举
 */
@Getter
public enum LoginStatus {
    SUCCESS(1, "成功"),
    FAILED(2, "失败");

    private final int code;
    private final String description;

    LoginStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static LoginStatus fromCode(int code) {
        for (LoginStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的登录状态代码: " + code);
    }
}