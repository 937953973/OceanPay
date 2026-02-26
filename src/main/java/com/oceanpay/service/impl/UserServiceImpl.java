package com.oceanpay.service.impl;

import com.oceanpay.entity.User;
import com.oceanpay.entity.UserPreference;
import com.oceanpay.enums.UserStatus;
import com.oceanpay.exception.BusinessException;
import com.oceanpay.exception.ErrorCode;
import com.oceanpay.repository.UserPreferenceRepository;
import com.oceanpay.repository.UserRepository;
import com.oceanpay.service.UserService;
import com.oceanpay.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    
    @Override
    public User getUserById(Long userId) {
        log.debug("根据ID获取用户: userId={}", userId);
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
    }
    
    @Override
    public User getUserByUsername(String username) {
        log.debug("根据用户名获取用户: username={}", username);
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
    }
    
    @Override
    public User getUserByEmail(String email) {
        log.debug("根据邮箱获取用户: email={}", email);
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
    }
    
    @Override
    public User getUserByPhone(String phone) {
        log.debug("根据手机号获取用户: phone={}", phone);
        
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
    }
    
    @Override
    public User getUserByIdentifier(String identifier) {
        log.debug("根据标识符获取用户: identifier={}", identifier);
        
        return userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在"));
    }
    
    @Override
    @Transactional
    public User createUser(User user) {
        log.info("创建用户: username={}, email={}", user.getUsername(), user.getEmail());
        
        // 验证用户数据
        validateUserData(user);
        
        // 保存用户
        User savedUser = userRepository.save(user);
        
        // 创建默认偏好设置
        createDefaultPreference(savedUser);
        
        log.info("用户创建成功: userId={}, username={}", savedUser.getId(), savedUser.getUsername());
        return savedUser;
    }
    
    @Override
    @Transactional
    public User updateUser(User user) {
        log.info("更新用户信息: userId={}", user.getId());
        
        // 检查用户是否存在
        User existingUser = getUserById(user.getId());
        
        // 更新允许修改的字段
        if (user.getFirstName() != null) {
            existingUser.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            existingUser.setLastName(user.getLastName());
        }
        if (user.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(user.getAvatarUrl());
        }
        if (user.getPhone() != null && !user.getPhone().equals(existingUser.getPhone())) {
            // 检查手机号是否可用
            if (!isPhoneAvailable(user.getPhone())) {
                throw new BusinessException(ErrorCode.USER_EXISTS.getCode(), "手机号已被使用");
            }
            existingUser.setPhone(user.getPhone());
            existingUser.setPhoneVerified(false); // 更换手机号需要重新验证
        }
        
        User updatedUser = userRepository.save(existingUser);
        log.info("用户信息更新成功: userId={}", updatedUser.getId());
        return updatedUser;
    }
    
    @Override
    @Transactional
    public void updateLastLoginTime(Long userId) {
        log.debug("更新用户最后登录时间: userId={}", userId);
        
        User user = getUserById(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    @Override
    public boolean isUsernameAvailable(String username) {
        log.debug("检查用户名是否可用: username={}", username);
        
        return !userRepository.existsByUsername(username);
    }
    
    @Override
    public boolean isEmailAvailable(String email) {
        log.debug("检查邮箱是否可用: email={}", email);
        
        return !userRepository.existsByEmail(email);
    }
    
    @Override
    public boolean isPhoneAvailable(String phone) {
        log.debug("检查手机号是否可用: phone={}", phone);
        
        if (phone == null || phone.trim().isEmpty()) {
            return true;
        }
        return !userRepository.existsByPhone(phone);
    }
    
    @Override
    public boolean verifyPassword(Long userId, String password) {
        log.debug("验证用户密码: userId={}", userId);
        
        User user = getUserById(userId);
        return PasswordUtil.matches(password, user.getPasswordHash());
    }
    
    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改用户密码: userId={}", userId);
        
        // 验证旧密码
        if (!verifyPassword(userId, oldPassword)) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR.getCode(), "旧密码错误");
        }
        
        // 验证新密码强度
        if (!PasswordUtil.validatePasswordStrength(newPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "新密码强度不足");
        }
        
        // 更新密码
        User user = getUserById(userId);
        user.setPasswordHash(PasswordUtil.encode(newPassword));
        userRepository.save(user);
        
        log.info("用户密码修改成功: userId={}", userId);
    }
    
    @Override
    @Transactional
    public void resetPassword(String target, String verificationCode, String newPassword) {
        log.info("重置用户密码: target={}", target);
        
        // 验证新密码强度
        if (!PasswordUtil.validatePasswordStrength(newPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "新密码强度不足");
        }
        
        // 根据目标类型查找用户
        User user;
        if (target.contains("@")) {
            user = getUserByEmail(target);
        } else {
            user = getUserByPhone(target);
        }
        
        // 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_DISABLED.getCode(), "用户已被禁用");
        }
        
        // 更新密码
        user.setPasswordHash(PasswordUtil.encode(newPassword));
        userRepository.save(user);
        
        log.info("用户密码重置成功: userId={}, target={}", user.getId(), target);
    }
    
    @Override
    @Transactional
    public void updateUserStatus(Long userId, UserStatus status) {
        log.info("更新用户状态: userId={}, status={}", userId, status);
        
        User user = getUserById(userId);
        user.setStatus(status);
        userRepository.save(user);
        
        log.info("用户状态更新成功: userId={}, status={}", userId, status);
    }
    
    @Override
    @Transactional
    public void verifyEmail(Long userId) {
        log.info("验证用户邮箱: userId={}", userId);
        
        User user = getUserById(userId);
        user.setEmailVerified(true);
        userRepository.save(user);
        
        log.info("用户邮箱验证成功: userId={}", userId);
    }
    
    @Override
    @Transactional
    public void verifyPhone(Long userId) {
        log.info("验证用户手机号: userId={}", userId);
        
        User user = getUserById(userId);
        user.setPhoneVerified(true);
        userRepository.save(user);
        
        log.info("用户手机号验证成功: userId={}", userId);
    }
    
    private void validateUserData(User user) {
        // 验证用户名
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "用户名不能为空");
        }
        
        // 验证邮箱
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "邮箱不能为空");
        }
        
        // 验证密码
        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "密码不能为空");
        }
        
        // 验证密码强度
        if (!PasswordUtil.validatePasswordStrength(user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "密码强度不足");
        }
    }
    
    private void createDefaultPreference(User user) {
        UserPreference preference = UserPreference.builder()
                .user(user)
                .defaultCurrency("USD")
                .defaultLanguage("en")
                .timezone("UTC")
                .notificationEmail(true)
                .notificationSms(false)
                .notificationPush(true)
                .marketingConsent(false)
                .build();
        
        userPreferenceRepository.save(preference);
        log.debug("创建用户默认偏好设置: userId={}", user.getId());
    }
}