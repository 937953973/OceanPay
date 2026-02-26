package com.oceanpay.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 手机号（可选）
     */
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度必须在8-100个字符之间")
    private String password;
    
    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    /**
     * 名字
     */
    @Size(max = 50, message = "名字长度不能超过50个字符")
    private String firstName;
    
    /**
     * 姓氏
     */
    @Size(max = 50, message = "姓氏长度不能超过50个字符")
    private String lastName;
    
    /**
     * 注册验证令牌
     */
    @NotBlank(message = "注册验证令牌不能为空")
    private String verificationToken;
    
    /**
     * 默认货币
     */
    private String defaultCurrency = "USD";
    
    /**
     * 默认语言
     */
    private String defaultLanguage = "en";
}