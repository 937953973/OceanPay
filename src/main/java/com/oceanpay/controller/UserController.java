package com.oceanpay.controller;

import com.oceanpay.dto.ApiResponse;
import com.oceanpay.dto.ChangePasswordRequest;
import com.oceanpay.dto.LoginResponse;
import com.oceanpay.entity.User;
import com.oceanpay.enums.UserStatus;
import com.oceanpay.exception.BusinessException;
import com.oceanpay.exception.ErrorCode;
import com.oceanpay.service.AuthService;
import com.oceanpay.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    
    private final UserService userService;
    private final AuthService authService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse.UserInfo> getCurrentUser(
            @RequestHeader("Authorization") String authorizationHeader) {
        
        log.debug("获取当前用户信息");
        
        // 验证令牌并获取用户
        User user = validateTokenAndGetUser(authorizationHeader);
        
        // 构建响应
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.fromUser(user);
        
        log.debug("获取当前用户信息成功: userId={}", user.getId());
        return ApiResponse.success(userInfo);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/me")
    public ApiResponse<LoginResponse.UserInfo> updateCurrentUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody User updateRequest) {
        
        log.info("更新用户信息请求: userId={}", updateRequest.getId());
        
        // 验证令牌并获取当前用户
        User currentUser = validateTokenAndGetUser(authorizationHeader);
        
        // 确保只能更新自己的信息
        if (!Objects.equals(currentUser.getId(), updateRequest.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "只能更新自己的信息");
        }
        
        // 更新用户信息
        User updatedUser = userService.updateUser(updateRequest);
        
        // 构建响应
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.fromUser(updatedUser);
        
        log.info("用户信息更新成功: userId={}", updatedUser.getId());
        return ApiResponse.success(userInfo);
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        log.info("修改密码请求");
        
        // 验证令牌并获取当前用户
        User currentUser = validateTokenAndGetUser(authorizationHeader);
        
        // 验证密码一致性
        if (!Objects.equals(request.getNewPassword(), request.getConfirmNewPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "两次输入的新密码不一致");
        }
        
        // 修改密码
        userService.changePassword(
                currentUser.getId(),
                request.getOldPassword(),
                request.getNewPassword()
        );
        
        log.info("密码修改成功: userId={}", currentUser.getId());
        return ApiResponse.success();
    }
    
    /**
     * 验证邮箱
     */
    @PostMapping("/me/email/verify")
    public ApiResponse<Void> verifyEmail(
            @RequestHeader("Authorization") String authorizationHeader) {
        
        log.info("验证邮箱请求");
        
        // 验证令牌并获取当前用户
        User currentUser = validateTokenAndGetUser(authorizationHeader);
        
        // 验证邮箱
        userService.verifyEmail(currentUser.getId());
        
        log.info("邮箱验证成功: userId={}", currentUser.getId());
        return ApiResponse.success();
    }
    
    /**
     * 验证手机号
     */
    @PostMapping("/me/phone/verify")
    public ApiResponse<Void> verifyPhone(
            @RequestHeader("Authorization") String authorizationHeader) {
        
        log.info("验证手机号请求");
        
        // 验证令牌并获取当前用户
        User currentUser = validateTokenAndGetUser(authorizationHeader);
        
        // 验证手机号
        userService.verifyPhone(currentUser.getId());
        
        log.info("手机号验证成功: userId={}", currentUser.getId());
        return ApiResponse.success();
    }
    
    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check/username")
    public ApiResponse<Boolean> checkUsernameAvailability(
            @RequestParam String username) {
        
        log.debug("检查用户名是否可用: username={}", username);
        
        boolean available = userService.isUsernameAvailable(username);
        
        log.debug("用户名可用性检查结果: username={}, available={}", username, available);
        return ApiResponse.success(available);
    }
    
    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check/email")
    public ApiResponse<Boolean> checkEmailAvailability(
            @RequestParam String email) {
        
        log.debug("检查邮箱是否可用: email={}", email);
        
        boolean available = userService.isEmailAvailable(email);
        
        log.debug("邮箱可用性检查结果: email={}, available={}", email, available);
        return ApiResponse.success(available);
    }
    
    /**
     * 检查手机号是否可用
     */
    @GetMapping("/check/phone")
    public ApiResponse<Boolean> checkPhoneAvailability(
            @RequestParam String phone) {
        
        log.debug("检查手机号是否可用: phone={}", phone);
        
        boolean available = userService.isPhoneAvailable(phone);
        
        log.debug("手机号可用性检查结果: phone={}, available={}", phone, available);
        return ApiResponse.success(available);
    }
    
    /**
     * 获取用户信息（管理员接口）
     */
    @GetMapping("/{userId}")
    public ApiResponse<LoginResponse.UserInfo> getUserById(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long userId) {
        
        log.debug("获取用户信息: userId={}", userId);
        
        // 验证令牌（这里简单验证，实际项目中需要检查权限）
        validateTokenAndGetUser(authorizationHeader);
        
        // 获取用户信息
        User user = userService.getUserById(userId);
        
        // 构建响应
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.fromUser(user);
        
        log.debug("获取用户信息成功: userId={}", userId);
        return ApiResponse.success(userInfo);
    }
    
    /**
     * 更新用户状态（管理员接口）
     */
    @PutMapping("/{userId}/status")
    public ApiResponse<Void> updateUserStatus(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long userId,
            @RequestParam UserStatus status) {
        
        log.info("更新用户状态: userId={}, status={}", userId, status);
        
        // 验证令牌（这里简单验证，实际项目中需要检查管理员权限）
        validateTokenAndGetUser(authorizationHeader);
        
        // 更新用户状态
        userService.updateUserStatus(userId, status);
        
        log.info("用户状态更新成功: userId={}, status={}", userId, status);
        return ApiResponse.success();
    }
    
    /**
     * 验证令牌并获取用户
     */
    private User validateTokenAndGetUser(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.TOKEN_MISSING.getCode(), "访问令牌缺失");
        }
        
        String token = authorizationHeader.substring(7);
        return authService.validateToken(token);
    }
}