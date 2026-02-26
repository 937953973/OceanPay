package com.oceanpay.entity;

import com.oceanpay.enums.AddressType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户地址实体类
 */
@Entity
@Table(name = "user_address", indexes = {
    @Index(name = "ua_user_id", columnList = "userId"),
    @Index(name = "ua_user_type", columnList = "userId, addressType"),
    @Index(name = "ua_country", columnList = "countryCode")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "address_type", nullable = false)
    @Convert(converter = AddressTypeConverter.class)
    private AddressType addressType;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;
    
    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;
    
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;
    
    @Column(name = "state", length = 100)
    private String state;
    
    @Column(name = "city", nullable = false, length = 100)
    private String city;
    
    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;
    
    @Column(name = "address_line2", length = 255)
    private String addressLine2;
    
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isDefault == null) {
            isDefault = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * AddressType JPA转换器
     */
    @Converter(autoApply = true)
    public static class AddressTypeConverter implements AttributeConverter<AddressType, Integer> {
        @Override
        public Integer convertToDatabaseColumn(AddressType type) {
            return type != null ? type.getCode() : null;
        }
        
        @Override
        public AddressType convertToEntityAttribute(Integer code) {
            return code != null ? AddressType.fromCode(code) : null;
        }
    }
}