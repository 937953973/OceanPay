package com.oceanpay.entity;

import com.oceanpay.enums.LoginType;
import com.oceanpay.enums.LoginStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户登录日志实体类
 */
@Entity
@Table(name = "user_login_log", indexes = {
    @Index(name = "ull_user_id", columnList = "userId"),
    @Index(name = "ull_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "login_type", nullable = false)
    @Convert(converter = LoginTypeConverter.class)
    private LoginType loginType;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "login_status", nullable = false)
    @Convert(converter = LoginStatusConverter.class)
    private LoginStatus loginStatus;
    
    @Column(name = "failure_reason", length = 100)
    private String failureReason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * LoginType JPA转换器
     */
    @Converter(autoApply = true)
    public static class LoginTypeConverter implements AttributeConverter<LoginType, Integer> {
        @Override
        public Integer convertToDatabaseColumn(LoginType type) {
            return type != null ? type.getCode() : null;
        }
        
        @Override
        public LoginType convertToEntityAttribute(Integer code) {
            return code != null ? LoginType.fromCode(code) : null;
        }
    }
    
    /**
     * LoginStatus JPA转换器
     */
    @Converter(autoApply = true)
    public static class LoginStatusConverter implements AttributeConverter<LoginStatus, Integer> {
        @Override
        public Integer convertToDatabaseColumn(LoginStatus status) {
            return status != null ? status.getCode() : null;
        }
        
        @Override
        public LoginStatus convertToEntityAttribute(Integer code) {
            return code != null ? LoginStatus.fromCode(code) : null;
        }
    }
}