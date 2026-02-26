package com.oceanpay.controller;

import com.oceanpay.dto.*;
import com.oceanpay.entity.User;
import com.oceanpay.enums.LoginType;
import com.oceanpay.enums.VerificationCodeType;
import com.oceanpay.exception.BusinessException;
import com.oceanpay.exception.ErrorCode;
import com.oceanpay.service.AuthService;
import com.oceanpay.service.UserService;
import com.oceanpay.util.VerificationCodeUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    
    /**
     * 密码登录
     */
    @PostMapping("/login/password")
    public ApiResponse<LoginResponse> loginWithPassword(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("密码登录请求: identifier={}", request.getIdentifier());
        
        // 验证登录类型
        if (!"PASSWORD".equalsIgnoreCase(request.getLoginType())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "登录类型错误");
        }
        
        // 获取客户端信息
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        // 执行登录
        User user = authService.loginWithPassword(
                request.getIdentifier(),
                request.getPassword(),
                ipAddress,
                userAgent
        );
        
        // 生成令牌
        String accessToken = authService.generateToken(user);
        String refreshToken = authService.generateRefreshToken(user);
        
        // 构建响应
        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600) // 1小时
                .user(LoginResponse.UserInfo.fromUser(user))
                .build();
        
        log.info("密码登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return ApiResponse.success(response);
    }
    
    /**
     * 验证码登录
     */
    @PostMapping("/login/verification-code")
    public ApiResponse<LoginResponse> loginWithVerificationCode(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("验证码登录请求: identifier={}", request.getIdentifier());
        
        // 验证登录类型
        if (!"VERIFICATION_CODE".equalsIgnoreCase(request.getLoginType())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "登录类型错误");
        }
        
        // 验证验证码
        if (request.getVerificationCode() == null || request.getVerificationCode().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "验证码不能为空");
        }
        
        // 获取客户端信息
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        // 执行登录
        User user = authService.loginWithVerificationCode(
                request.getIdentifier(),
                request.getVerificationCode(),
                ipAddress,
                userAgent
        );
        
        // 生成令牌
        String accessToken = authService.generateToken(user);
        String refreshToken = authService.generateRefreshToken(user);
        
        // 构建响应
        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600) // 1小时
                .user(LoginResponse.UserInfo.fromUser(user))
                .build();
        
        log.info("验证码登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return ApiResponse.success(response);
    }
    
    /**
     * 发送注册验证码
     */
    @PostMapping("/register/send-code")
    public ApiResponse<Void> sendRegisterVerificationCode(
            @Valid @RequestBody SendVerificationCodeRequest request) {
        
        log.info("发送注册验证码请求: target={}, codeType={}", request.getTarget(), request.getCodeType());
        
        // 验证验证码类型
        if (!"REGISTER".equalsIgnoreCase(request.getCodeType())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "验证码类型错误");
        }
        
        // 发送验证码
        authService.sendRegisterVerificationCode(request.getTarget());
        
        log.info("注册验证码发送成功: target={}", request.getTarget());
        return ApiResponse.success();
    }
    
    /**
     * 验证注册验证码
     */
    @PostMapping("/register/verify-code")
    public ApiResponse<String> verifyRegisterVerificationCode(
            @Valid @RequestBody VerifyCodeRequest request) {
        
        log.info("验证注册验证码请求: target={}, codeType={}", request.getTarget(), request.getCodeType());
        
        // 验证验证码类型
        if (!"REGISTER".equalsIgnoreCase(request.getCodeType())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "验证码类型错误");
        }
        
        // 验证验证码并获取令牌
        String verificationToken = authService.verifyRegisterVerificationCode(
                request.getTarget(),
                request.getCode()
        );
        
        log.info("注册验证码验证成功: target={}", request.getTarget());
        return ApiResponse.success(verificationToken);
    }
    
    /**
     * 注册用户
     */
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("用户注册请求: username={}, email={}", request.getUsername(), request.getEmail());
        
        // 验证密码一致性
        if (!Objects.equals(request.getPassword(), request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "两次输入的密码不一致");
        }
        
        // 创建用户实体
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(request.getPassword()) // 注意：这里存储的是明文密码，服务层会加密
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
        
        // 注册用户
        User registeredUser = authService.register(
                request.getVerificationToken(),
                user,
                request.getDefaultCurrency(),
                request.getDefaultLanguage()
        );
        
        // 获取客户端信息
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        // 记录登录日志（模拟自动登录）
        log.info("用户注册成功并自动登录: userId={}, username={}", registeredUser.getId(), registeredUser.getUsername());
        
        // 生成令牌
        String accessToken = authService.generateToken(registeredUser);
        String refreshToken = authService.generateRefreshToken(registeredUser);
        
        // 构建响应
        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600) // 1小时
                .user(LoginResponse.UserInfo.fromUser(registeredUser))
                .build();
        
        return ApiResponse.success(response);
    }
    
    /**
     * 发送登录验证码
     */
    @PostMapping("/login/send-code")
    public ApiResponse<Void> sendLoginVerificationCode(
            @Valid @RequestBody SendVerificationCodeRequest request) {
        
        log.info("发送登录验证码请求: target={}, codeType={}", request.getTarget(), request.getCodeType());
        
        // 验证验证码类型
        if (!"LOGIN".equalsIgnoreCase(request.getCodeType())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "验证码类型错误");
        }
        
        // 发送验证码
        authService.sendLoginVerificationCode(request.getTarget());
        
        log.info("登录验证码发送成功: target={}", request.getTarget());
        return ApiResponse.success();
    }
    
    /**
     * 发送重置密码验证码
     */
    @PostMapping("/password/reset/send-code")
    public ApiResponse<Void> sendResetPasswordVerificationCode(
            @Valid @RequestBody SendVerificationCodeRequest request) {
        
        log.info("发送重置密码验证码请求: target={}, codeType={}", request.getTarget(), request.getCodeType());
        
        // 验证验证码类型
        if (!"RESET_PASSWORD".equalsIgnoreCase(request.getCodeType())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "验证码类型错误");
        }
        
        // 发送验证码
        authService.sendResetPasswordVerificationCode(request.getTarget());
        
        log.info("重置密码验证码发送成功: target={}", request.getTarget());
        return ApiResponse.success();
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        log.info("重置密码请求: target={}", request.getTarget());
        
        // 验证密码一致性
        if (!Objects.equals(request.getNewPassword(), request.getConfirmNewPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "两次输入的新密码不一致");
        }
        
        // 重置密码
        userService.resetPassword(
                request.getTarget(),
                request.getVerificationCode(),
                request.getNewPassword()
        );
        
        log.info("密码重置成功: target={}", request.getTarget());
        return ApiResponse.success();
    }
    
    /**
     * 刷新令牌
     */
    @PostMapping("/refresh-token")
    public ApiResponse<LoginResponse> refreshToken(
            @RequestHeader("Authorization") String authorizationHeader) {
        
        log.info("刷新令牌请求");
        
        // 提取刷新令牌
        String refreshToken = extractTokenFromHeader(authorizationHeader);
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.TOKEN_MISSING.getCode(), "刷新令牌缺失");
        }
        
        // 刷新令牌
        String newAccessToken = authService.refreshToken(refreshToken);
        
        // 解析新令牌获取用户信息
        User user = authService.validateToken(newAccessToken);
        
        // 生成新的刷新令牌
        String newRefreshToken = authService.generateRefreshToken(user);
        
        // 构建响应
        LoginResponse response = LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(3600) // 1小时
                .user(LoginResponse.UserInfo.fromUser(user))
                .build();
        
        log.info("令牌刷新成功: userId={}", user.getId());
        return ApiResponse.success(response);
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String authorizationHeader) {
        
        log.info("退出登录请求");
        
        // 提取访问令牌
        String accessToken = extractTokenFromHeader(authorizationHeader);
        if (accessToken == null) {
            throw new BusinessException(ErrorCode.TOKEN_MISSING.getCode(), "访问令牌缺失");
        }
        
        // 退出登录
        authService.logout(accessToken);
        
        log.info("退出登录成功");
        return ApiResponse.success();
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // 多个代理时，第一个IP为客户端真实IP
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress;
    }
    
    /**
     * 从Authorization头中提取令牌
     */
    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7);
    }
}