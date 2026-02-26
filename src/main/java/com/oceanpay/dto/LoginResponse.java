package com.oceanpay.dto;

import com.oceanpay.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 令牌类型
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * 访问令牌过期时间（秒）
     */
    private Integer expiresIn;
    
    /**
     * 用户信息
     */
    private UserInfo user;
    
    /**
     * 用户信息DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private Boolean emailVerified;
        private Boolean phoneVerified;
        
        public static UserInfo fromUser(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .avatarUrl(user.getAvatarUrl())
                    .emailVerified(user.getEmailVerified())
                    .phoneVerified(user.getPhoneVerified())
                    .build();
        }
    }
}