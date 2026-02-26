package com.oceanpay.service;

import com.oceanpay.entity.User;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 密码登录
     */
    User loginWithPassword(String identifier, String password, String ipAddress, String userAgent);
    
    /**
     * 验证码登录
     */
    User loginWithVerificationCode(String target, String code, String ipAddress, String userAgent);
    
    /**
     * 发送注册验证码
     */
    void sendRegisterVerificationCode(String target);
    
    /**
     * 验证注册验证码
     */
    String verifyRegisterVerificationCode(String target, String code);
    
    /**
     * 注册用户
     */
    User register(String verificationToken, User user, String defaultCurrency, String defaultLanguage);
    
    /**
     * 发送登录验证码
     */
    void sendLoginVerificationCode(String target);
    
    /**
     * 发送重置密码验证码
     */
    void sendResetPasswordVerificationCode(String target);
    
    /**
     * 刷新Token
     */
    String refreshToken(String refreshToken);
    
    /**
     * 退出登录
     */
    void logout(String token);
    
    /**
     * 验证Token
     */
    User validateToken(String token);
    
    /**
     * 生成Token
     */
    String generateToken(User user);
    
    /**
     * 生成刷新Token
     */
    String generateRefreshToken(User user);
}