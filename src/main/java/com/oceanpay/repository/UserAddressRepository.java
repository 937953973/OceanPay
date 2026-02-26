package com.oceanpay.repository;

import com.oceanpay.entity.UserAddress;
import com.oceanpay.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户地址数据访问接口
 */
@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    
    /**
     * 根据用户ID查找地址列表
     */
    List<UserAddress> findByUserId(Long userId);
    
    /**
     * 根据用户ID和地址类型查找地址列表
     */
    List<UserAddress> findByUserIdAndAddressType(Long userId, AddressType addressType);
    
    /**
     * 根据用户ID查找默认地址
     */
    Optional<UserAddress> findByUserIdAndIsDefaultTrue(Long userId);
    
    /**
     * 根据用户ID和地址类型查找默认地址
     */
    Optional<UserAddress> findByUserIdAndAddressTypeAndIsDefaultTrue(Long userId, AddressType addressType);
    
    /**
     * 检查用户是否有默认地址
     */
    boolean existsByUserIdAndIsDefaultTrue(Long userId);
    
    /**
     * 检查用户是否有指定类型的默认地址
     */
    boolean existsByUserIdAndAddressTypeAndIsDefaultTrue(Long userId, AddressType addressType);
    
    /**
     * 清除用户的所有默认地址
     */
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefault = false WHERE ua.user.id = :userId")
    void clearDefaultAddresses(@Param("userId") Long userId);
    
    /**
     * 清除用户指定类型的默认地址
     */
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefault = false WHERE ua.user.id = :userId AND ua.addressType = :addressType")
    void clearDefaultAddressesByType(@Param("userId") Long userId, @Param("addressType") AddressType addressType);
    
    /**
     * 统计用户的地址数量
     */
    long countByUserId(Long userId);
}