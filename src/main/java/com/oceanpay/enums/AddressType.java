package com.oceanpay.enums;

/**
 * 地址类型枚举
 */
public enum AddressType {
    SHIPPING(1, "收货地址"),
    INVOICE(2, "发票地址");

    private final int code;
    private final String description;

    AddressType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AddressType fromCode(int code) {
        for (AddressType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的地址类型代码: " + code);
    }
}