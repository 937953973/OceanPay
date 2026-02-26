package com.oceanpay.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 验证码工具类
 */
public class VerificationCodeUtil {
    
    private static final SecureRandom random = new SecureRandom();
    private static final String DIGITS = "0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRE_MINUTES = 5;
    
    /**
     * 生成随机验证码
     */
    public static String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        return code.toString();
    }
    
    /**
     * 生成过期时间
     */
    public static LocalDateTime generateExpireTime() {
        return LocalDateTime.now().plus(EXPIRE_MINUTES, ChronoUnit.MINUTES);
    }
    
    /**
     * 检查验证码是否过期
     */
    public static boolean isExpired(LocalDateTime expireTime) {
        return LocalDateTime.now().isAfter(expireTime);
    }
    
    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * 验证手机号格式（国际格式）
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // 简单的国际手机号验证，支持+开头
        String phoneRegex = "^\\+?[1-9]\\d{1,14}$";
        return phone.matches(phoneRegex);
    }
    
    /**
     * 判断目标类型（邮箱或手机号）
     */
    public static String getTargetType(String target) {
        if (isValidEmail(target)) {
            return "EMAIL";
        } else if (isValidPhone(target)) {
            return "PHONE";
        } else {
            return "UNKNOWN";
        }
    }
}