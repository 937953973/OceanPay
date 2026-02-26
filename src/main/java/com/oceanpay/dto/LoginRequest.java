package com.oceanpay.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {
    
    /**
     * 登录标识符（用户名/邮箱/手机号）
     */
    @NotBlank(message = "登录标识符不能为空")
    private String identifier;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 登录类型：PASSWORD - 密码登录，VERIFICATION_CODE - 验证码登录
     */
    @NotBlank(message = "登录类型不能为空")
    private String loginType;
    
    /**
     * 验证码（验证码登录时必填）
     */
    private String verificationCode;
    
    /**
     * 客户端IP地址
     */
    private String ipAddress;
    
    /**
     * 用户代理
     */
    private String userAgent;
}