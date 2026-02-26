package com.oceanpay.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户偏好设置实体类
 */
@Entity
@Table(name = "user_preference")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrency;
    
    @Column(name = "default_language", nullable = false, length = 10)
    private String defaultLanguage;
    
    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone;
    
    @Column(name = "notification_email", nullable = false)
    private Boolean notificationEmail;
    
    @Column(name = "notification_sms", nullable = false)
    private Boolean notificationSms;
    
    @Column(name = "notification_push", nullable = false)
    private Boolean notificationPush;
    
    @Column(name = "marketing_consent", nullable = false)
    private Boolean marketingConsent;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (defaultCurrency == null) {
            defaultCurrency = "USD";
        }
        if (defaultLanguage == null) {
            defaultLanguage = "en";
        }
        if (timezone == null) {
            timezone = "UTC";
        }
        if (notificationEmail == null) {
            notificationEmail = true;
        }
        if (notificationSms == null) {
            notificationSms = false;
        }
        if (notificationPush == null) {
            notificationPush = true;
        }
        if (marketingConsent == null) {
            marketingConsent = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}