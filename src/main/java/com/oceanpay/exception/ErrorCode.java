package com.oceanpay.exception;

/**
 * 错误码枚举
 */
public enum ErrorCode {
    
    // 通用错误码
    SUCCESS("00000", "成功"),
    SYSTEM_ERROR("99999", "系统错误"),
    PARAM_ERROR("A0001", "参数错误"),
    UNAUTHORIZED("A0002", "未授权"),
    FORBIDDEN("A0003", "禁止访问"),
    NOT_FOUND("A0004", "资源不存在"),
    CONFLICT("A0005", "资源冲突"),
    TOO_MANY_REQUESTS("A0006", "请求过于频繁"),
    
    // 用户相关错误码
    USER_NOT_FOUND("U0001", "用户不存在"),
    USER_DISABLED("U0002", "用户已被禁用"),
    USER_EXISTS("U0003", "用户已存在"),
    USER_PASSWORD_ERROR("U0004", "密码错误"),
    USER_LOGIN_FAILED("U0005", "登录失败"),
    USER_LOGIN_LOCKED("U0006", "登录被锁定，请稍后重试"),
    
    // 验证码相关错误码
    VERIFICATION_CODE_NOT_FOUND("V0001", "验证码不存在"),
    VERIFICATION_CODE_EXPIRED("V0002", "验证码已过期"),
    VERIFICATION_CODE_USED("V0003", "验证码已使用"),
    VERIFICATION_CODE_ERROR("V0004", "验证码错误"),
    VERIFICATION_CODE_SEND_FAILED("V0005", "验证码发送失败"),
    VERIFICATION_CODE_LIMIT("V0006", "验证码发送过于频繁"),
    
    // 认证相关错误码
    TOKEN_EXPIRED("T0001", "Token已过期"),
    TOKEN_INVALID("T0002", "Token无效"),
    TOKEN_MISSING("T0003", "Token缺失"),
    
    // 地址相关错误码
    ADDRESS_NOT_FOUND("D0001", "地址不存在"),
    ADDRESS_LIMIT("D0002", "地址数量达到上限"),
    
    // 权限相关错误码
    PERMISSION_DENIED("P0001", "权限不足");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}