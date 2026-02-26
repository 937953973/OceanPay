package com.oceanpay.service;

import com.oceanpay.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 根据ID获取用户
     */
    User getUserById(Long userId);
    
    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);
    
    /**
     * 根据邮箱获取用户
     */
    User getUserByEmail(String email);
    
    /**
     * 根据手机号获取用户
     */
    User getUserByPhone(String phone);
    
    /**
     * 根据标识符（用户名/邮箱/手机号）获取用户
     */
    User getUserByIdentifier(String identifier);
    
    /**
     * 创建用户
     */
    User createUser(User user);
    
    /**
     * 更新用户信息
     */
    User updateUser(User user);
    
    /**
     * 更新用户最后登录时间
     */
    void updateLastLoginTime(Long userId);
    
    /**
     * 检查用户名是否可用
     */
    boolean isUsernameAvailable(String username);
    
    /**
     * 检查邮箱是否可用
     */
    boolean isEmailAvailable(String email);
    
    /**
     * 检查手机号是否可用
     */
    boolean isPhoneAvailable(String phone);
    
    /**
     * 验证用户密码
     */
    boolean verifyPassword(Long userId, String password);
    
    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 重置密码
     */
    void resetPassword(String target, String verificationCode, String newPassword);
    
    /**
     * 更新用户状态
     */
    void updateUserStatus(Long userId, com.oceanpay.enums.UserStatus status);
    
    /**
     * 验证邮箱
     */
    void verifyEmail(Long userId);
    
    /**
     * 验证手机号
     */
    void verifyPhone(Long userId);
}