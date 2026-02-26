package com.oceanpay.repository;

import com.oceanpay.entity.VerificationCode;
import com.oceanpay.enums.VerificationCodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 验证码数据访问接口
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    
    /**
     * 根据目标、验证码类型和是否已使用查找最新的验证码
     */
    Optional<VerificationCode> findFirstByTargetAndCodeTypeAndUsedFalseOrderByCreatedAtDesc(
            String target, VerificationCodeType codeType);
    
    /**
     * 根据目标、验证码类型、验证码和是否已使用查找验证码
     */
    Optional<VerificationCode> findByTargetAndCodeTypeAndCodeAndUsedFalse(
            String target, VerificationCodeType codeType, String code);
    
    /**
     * 统计指定时间内目标发送的验证码数量
     */
    @Query("SELECT COUNT(vc) FROM VerificationCode vc WHERE vc.target = :target AND vc.codeType = :codeType AND vc.createdAt >= :startTime")
    long countByTargetAndCodeTypeAndCreatedAtAfter(
            @Param("target") String target,
            @Param("codeType") VerificationCodeType codeType,
            @Param("startTime") LocalDateTime startTime);
    
    /**
     * 标记验证码为已使用
     */
    @Modifying
    @Query("UPDATE VerificationCode vc SET vc.used = true WHERE vc.id = :id")
    void markAsUsed(@Param("id") Long id);
    
    /**
     * 删除过期的验证码
     */
    @Modifying
    @Query("DELETE FROM VerificationCode vc WHERE vc.expiresAt < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);
    
    /**
     * 统计指定时间内IP地址发送的验证码数量
     */
    @Query("SELECT COUNT(vc) FROM VerificationCode vc WHERE vc.createdAt >= :startTime")
    long countByCreatedAtAfter(@Param("startTime") LocalDateTime startTime);
}