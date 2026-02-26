package com.oceanpay.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 验证验证码请求DTO
 */
@Data
public class VerifyCodeRequest {
    
    /**
     * 目标（邮箱或手机号）
     */
    @NotBlank(message = "目标不能为空")
    private String target;
    
    /**
     * 验证码类型：REGISTER - 注册，LOGIN - 登录，RESET_PASSWORD - 重置密码
     */
    @NotBlank(message = "验证码类型不能为空")
    private String codeType;
    
    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;
}