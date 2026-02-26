package com.oceanpay.repository;

import com.oceanpay.entity.UserLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户登录日志数据访问接口
 */
@Repository
public interface UserLoginLogRepository extends JpaRepository<UserLoginLog, Long> {
    
    /**
     * 根据用户ID查找登录日志
     */
    List<UserLoginLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 根据用户ID和时间范围查找登录日志
     */
    List<UserLoginLog> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计用户指定时间内的失败登录次数
     */
    @Query("SELECT COUNT(ll) FROM UserLoginLog ll WHERE ll.user.id = :userId AND ll.loginStatus = com.oceanpay.enums.LoginStatus.FAILED AND ll.createdAt >= :startTime")
    long countFailedLoginsSince(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime);
    
    /**
     * 统计IP地址指定时间内的失败登录次数
     */
    @Query("SELECT COUNT(ll) FROM UserLoginLog ll WHERE ll.ipAddress = :ipAddress AND ll.loginStatus = com.oceanpay.enums.LoginStatus.FAILED AND ll.createdAt >= :startTime")
    long countFailedLoginsByIpSince(
            @Param("ipAddress") String ipAddress,
            @Param("startTime") LocalDateTime startTime);
    
    /**
     * 删除过期的登录日志
     */
    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}