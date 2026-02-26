package com.oceanpay.enums;

/**
 * 用户状态枚举
 */
public enum UserStatus {
    ACTIVE(1, "正常"),
    FROZEN(2, "冻结"),
    DELETED(3, "注销");

    private final int code;
    private final String description;

    UserStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UserStatus fromCode(int code) {
        for (UserStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的用户状态代码: " + code);
    }
}