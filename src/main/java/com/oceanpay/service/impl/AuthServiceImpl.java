package com.oceanpay.service.impl;

import com.oceanpay.entity.User;
import com.oceanpay.entity.UserLoginLog;
import com.oceanpay.entity.VerificationCode;
import com.oceanpay.enums.LoginStatus;
import com.oceanpay.enums.LoginType;
import com.oceanpay.enums.UserStatus;
import com.oceanpay.enums.VerificationCodeType;
import com.oceanpay.exception.BusinessException;
import com.oceanpay.exception.ErrorCode;
import com.oceanpay.repository.UserLoginLogRepository;
import com.oceanpay.repository.UserRepository;
import com.oceanpay.repository.VerificationCodeRepository;
import com.oceanpay.service.AuthService;
import com.oceanpay.service.UserService;
import com.oceanpay.service.VerificationCodeService;
import com.oceanpay.util.JwtUtil;
import com.oceanpay.util.PasswordUtil;
import com.oceanpay.util.VerificationCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final UserService userService;
    private final VerificationCodeService verificationCodeService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final UserLoginLogRepository userLoginLogRepository;
    private final JwtUtil jwtUtil;
    
    @Override
    @Transactional
    public User loginWithPassword(String identifier, String password, String ipAddress, String userAgent) {
        log.info("用户密码登录: identifier={}, ipAddress={}", identifier, ipAddress);
        
        // 查找用户
        User user = userService.getUserByIdentifier(identifier);
        if (user == null) {
            logLoginFailure(null, identifier, LoginType.PASSWORD, ipAddress, userAgent, "用户不存在");
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            logLoginFailure(user.getId(), identifier, LoginType.PASSWORD, ipAddress, userAgent, "用户状态异常");
            throw new BusinessException(ErrorCode.USER_DISABLED.getCode(), "用户已被禁用");
        }
        
        // 验证密码
        if (!PasswordUtil.matches(password, user.getPasswordHash())) {
            logLoginFailure(user.getId(), identifier, LoginType.PASSWORD, ipAddress, userAgent, "密码错误");
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR.getCode(), "密码错误");
        }
        
        // 更新最后登录时间
        userService.updateLastLoginTime(user.getId());
        
        // 记录登录成功日志
        logLoginSuccess(user.getId(), LoginType.PASSWORD, ipAddress, userAgent);
        
        log.info("用户密码登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return user;
    }
    
    @Override
    @Transactional
    public User loginWithVerificationCode(String target, String code, String ipAddress, String userAgent) {
        log.info("用户验证码登录: target={}, ipAddress={}", target, ipAddress);
        
        // 验证验证码
        if (!verificationCodeService.verifyAndUseCode(target, VerificationCodeType.LOGIN, code)) {
            logLoginFailure(null, target, LoginType.VERIFICATION_CODE, ipAddress, userAgent, "验证码错误");
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR.getCode(), "验证码错误");
        }
        
        // 根据目标类型查找用户
        User user;
        String targetType = VerificationCodeUtil.getTargetType(target);
        if ("EMAIL".equals(targetType)) {
            user = userService.getUserByEmail(target);
        } else if ("PHONE".equals(targetType)) {
            user = userService.getUserByPhone(target);
        } else {
            logLoginFailure(null, target, LoginType.VERIFICATION_CODE, ipAddress, userAgent, "目标类型错误");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "目标类型错误");
        }
        
        if (user == null) {
            logLoginFailure(null, target, LoginType.VERIFICATION_CODE, ipAddress, userAgent, "用户不存在");
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            logLoginFailure(user.getId(), target, LoginType.VERIFICATION_CODE, ipAddress, userAgent, "用户状态异常");
            throw new BusinessException(ErrorCode.USER_DISABLED.getCode(), "用户已被禁用");
        }
        
        // 更新最后登录时间
        userService.updateLastLoginTime(user.getId());
        
        // 记录登录成功日志
        logLoginSuccess(user.getId(), LoginType.VERIFICATION_CODE, ipAddress, userAgent);
        
        log.info("用户验证码登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return user;
    }
    
    @Override
    public void sendRegisterVerificationCode(String target) {
        log.info("发送注册验证码: target={}", target);
        
        // 检查目标是否已注册
        String targetType = VerificationCodeUtil.getTargetType(target);
        if ("EMAIL".equals(targetType)) {
            if (!userService.isEmailAvailable(target)) {
                throw new BusinessException(ErrorCode.USER_EXISTS.getCode(), "邮箱已被注册");
            }
        } else if ("PHONE".equals(targetType)) {
            if (!userService.isPhoneAvailable(target)) {
                throw new BusinessException(ErrorCode.USER_EXISTS.getCode(), "手机号已被注册");
            }
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "目标类型错误");
        }
        
        // 发送验证码
        verificationCodeService.sendVerificationCode(target, VerificationCodeType.REGISTER);
    }
    
    @Override
    public String verifyRegisterVerificationCode(String target, String code) {
        log.info("验证注册验证码: target={}", target);
        
        // 验证验证码
        if (!verificationCodeService.verifyAndUseCode(target, VerificationCodeType.REGISTER, code)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR.getCode(), "验证码错误");
        }
        
        // 生成注册令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("target", target);
        claims.put("code", code);
        claims.put("type", "REGISTER");
        
        return jwtUtil.generateToken(claims, 1800); // 30分钟有效期
    }
    
    @Override
    @Transactional
    public User register(String verificationToken, User user, String defaultCurrency, String defaultLanguage) {
        log.info("用户注册: username={}, email={}", user.getUsername(), user.getEmail());
        
        // 验证注册令牌
        Map<String, Object> claims = jwtUtil.parseToken(verificationToken);
        if (claims == null || !"REGISTER".equals(claims.get("type"))) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID.getCode(), "注册令牌无效");
        }
        
        String target = (String) claims.get("target");
        String code = (String) claims.get("code");
        
        // 验证目标与用户信息匹配
        if (!target.equals(user.getEmail()) && !target.equals(user.getPhone())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "注册信息不匹配");
        }
        
        // 检查用户名是否可用
        if (!userService.isUsernameAvailable(user.getUsername())) {
            throw new BusinessException(ErrorCode.USER_EXISTS.getCode(), "用户名已被使用");
        }
        
        // 检查邮箱是否可用
        if (!userService.isEmailAvailable(user.getEmail())) {
            throw new BusinessException(ErrorCode.USER_EXISTS.getCode(), "邮箱已被注册");
        }
        
        // 检查手机号是否可用（如果有）
        if (user.getPhone() != null && !user.getPhone().isEmpty() && !userService.isPhoneAvailable(user.getPhone())) {
            throw new BusinessException(ErrorCode.USER_EXISTS.getCode(), "手机号已被注册");
        }
        
        // 加密密码
        user.setPasswordHash(PasswordUtil.encode(user.getPasswordHash()));
        
        // 设置默认值
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        
        // 保存用户
        User savedUser = userService.createUser(user);
        
        log.info("用户注册成功: userId={}, username={}", savedUser.getId(), savedUser.getUsername());
        return savedUser;
    }
    
    @Override
    public void sendLoginVerificationCode(String target) {
        log.info("发送登录验证码: target={}", target);
        
        // 检查用户是否存在
        String targetType = VerificationCodeUtil.getTargetType(target);
        User user = null;
        if ("EMAIL".equals(targetType)) {
            user = userService.getUserByEmail(target);
        } else if ("PHONE".equals(targetType)) {
            user = userService.getUserByPhone(target);
        }
        
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_DISABLED.getCode(), "用户已被禁用");
        }
        
        // 发送验证码
        verificationCodeService.sendVerificationCode(target, VerificationCodeType.LOGIN);
    }
    
    @Override
    public void sendResetPasswordVerificationCode(String target) {
        log.info("发送重置密码验证码: target={}", target);
        
        // 检查用户是否存在
        String targetType = VerificationCodeUtil.getTargetType(target);
        User user = null;
        if ("EMAIL".equals(targetType)) {
            user = userService.getUserByEmail(target);
        } else if ("PHONE".equals(targetType)) {
            user = userService.getUserByPhone(target);
        }
        
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_DISABLED.getCode(), "用户已被禁用");
        }
        
        // 发送验证码
        verificationCodeService.sendVerificationCode(target, VerificationCodeType.RESET_PASSWORD);
    }
    
    @Override
    public String refreshToken(String refreshToken) {
        log.info("刷新Token");
        
        // 验证刷新令牌
        Map<String, Object> claims = jwtUtil.parseToken(refreshToken);
        if (claims == null || !"REFRESH".equals(claims.get("type"))) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID.getCode(), "刷新令牌无效");
        }
        
        Long userId = Long.valueOf(claims.get("userId").toString());
        User user = userService.getUserById(userId);
        
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_DISABLED.getCode(), "用户已被禁用");
        }
        
        // 生成新的访问令牌
        return generateToken(user);
    }
    
    @Override
    public void logout(String token) {
        log.info("用户退出登录");
        // 在实际应用中，可以将令牌加入黑名单
        // 这里简单记录日志
        log.info("Token已失效: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
    }
    
    @Override
    public User validateToken(String token) {
        log.debug("验证Token");
        
        Map<String, Object> claims = jwtUtil.parseToken(token);
        if (claims == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID.getCode(), "Token无效");
        }
        
        Long userId = Long.valueOf(claims.get("userId").toString());
        User user = userService.getUserById(userId);
        
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_DISABLED.getCode(), "用户已被禁用");
        }
        
        return user;
    }
    
    @Override
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("type", "ACCESS");
        
        return jwtUtil.generateToken(claims, 3600); // 1小时有效期
    }
    
    @Override
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("type", "REFRESH");
        
        return jwtUtil.generateToken(claims, 604800); // 7天有效期
    }
    
    private void logLoginSuccess(Long userId, LoginType loginType, String ipAddress, String userAgent) {
        UserLoginLog log = UserLoginLog.builder()
                .user(User.builder().id(userId).build())
                .loginType(loginType)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginStatus(LoginStatus.SUCCESS)
                .build();
        
        userLoginLogRepository.save(log);
    }
    
    private void logLoginFailure(Long userId, String identifier, LoginType loginType, 
                                String ipAddress, String userAgent, String failureReason) {
        UserLoginLog log = UserLoginLog.builder()
                .user(userId != null ? User.builder().id(userId).build() : null)
                .loginType(loginType)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginStatus(LoginStatus.FAILED)
                .failureReason(failureReason)
                .build();
        
        userLoginLogRepository.save(log);
    }
}