package com.oceanpay.repository;

import com.oceanpay.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户偏好设置数据访问接口
 */
@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    
    /**
     * 根据用户ID查找偏好设置
     */
    Optional<UserPreference> findByUserId(Long userId);
    
    /**
     * 检查用户是否有偏好设置
     */
    boolean existsByUserId(Long userId);
    
    /**
     * 根据用户ID删除偏好设置
     */
    void deleteByUserId(Long userId);
}