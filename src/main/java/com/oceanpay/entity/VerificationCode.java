package com.oceanpay.entity;

import com.oceanpay.enums.VerificationCodeType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 验证码实体类
 */
@Entity
@Table(name = "verification_code", indexes = {
    @Index(name = "vc_target_type", columnList = "target, codeType, used"),
    @Index(name = "vc_expires_at", columnList = "expiresAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "target", nullable = false, length = 100)
    private String target;
    
    @Column(name = "code_type", nullable = false)
    @Convert(converter = VerificationCodeTypeConverter.class)
    private VerificationCodeType codeType;
    
    @Column(name = "code", nullable = false, length = 10)
    private String code;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "used", nullable = false)
    private Boolean used;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (used == null) {
            used = false;
        }
    }
    
    /**
     * VerificationCodeType JPA转换器
     */
    @Converter(autoApply = true)
    public static class VerificationCodeTypeConverter implements AttributeConverter<VerificationCodeType, Integer> {
        @Override
        public Integer convertToDatabaseColumn(VerificationCodeType type) {
            return type != null ? type.getCode() : null;
        }
        
        @Override
        public VerificationCodeType convertToEntityAttribute(Integer code) {
            return code != null ? VerificationCodeType.fromCode(code) : null;
        }
    }
}