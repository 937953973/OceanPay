package com.oceanpay.service;

import com.oceanpay.enums.VerificationCodeType;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {
    
    /**
     * 发送验证码
     */
    void sendVerificationCode(String target, VerificationCodeType codeType);
    
    /**
     * 验证验证码
     */
    boolean verifyCode(String target, VerificationCodeType codeType, String code);
    
    /**
     * 验证验证码并标记为已使用
     */
    boolean verifyAndUseCode(String target, VerificationCodeType codeType, String code);
    
    /**
     * 检查验证码发送频率限制
     */
    boolean checkSendFrequency(String target, VerificationCodeType codeType);
    
    /**
     * 检查验证码尝试次数限制
     */
    boolean checkAttemptFrequency(String target, VerificationCodeType codeType);
    
    /**
     * 清理过期的验证码
     */
    void cleanExpiredCodes();
}