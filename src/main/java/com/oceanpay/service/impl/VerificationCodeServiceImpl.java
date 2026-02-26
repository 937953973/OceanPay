package com.oceanpay.service.impl;

import com.oceanpay.entity.VerificationCode;
import com.oceanpay.enums.VerificationCodeType;
import com.oceanpay.exception.BusinessException;
import com.oceanpay.exception.ErrorCode;
import com.oceanpay.repository.VerificationCodeRepository;
import com.oceanpay.service.VerificationCodeService;
import com.oceanpay.util.VerificationCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 验证码服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {
    
    private final VerificationCodeRepository verificationCodeRepository;
    
    // 发送频率限制：60秒内只能发送1次
    private static final int SEND_FREQUENCY_SECONDS = 60;
    
    // 尝试次数限制：5分钟内最多尝试5次
    private static final int ATTEMPT_FREQUENCY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    
    @Override
    @Transactional
    public void sendVerificationCode(String target, VerificationCodeType codeType) {
        log.info("发送验证码: target={}, codeType={}", target, codeType);
        
        // 验证目标格式
        if (!isValidTarget(target)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "目标格式错误");
        }
        
        // 检查发送频率限制
        if (!checkSendFrequency(target, codeType)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_LIMIT.getCode(), "验证码发送过于频繁，请稍后重试");
        }
        
        // 生成验证码
        String code = VerificationCodeUtil.generateCode();
        LocalDateTime expiresAt = VerificationCodeUtil.generateExpireTime();
        
        // 保存验证码
        VerificationCode verificationCode = VerificationCode.builder()
                .target(target)
                .codeType(codeType)
                .code(code)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        
        verificationCodeRepository.save(verificationCode);
        
        // 发送验证码（这里模拟发送，实际项目中需要集成短信/邮件服务）
        sendCodeToTarget(target, code, codeType);
        
        log.info("验证码发送成功: target={}, codeType={}, code={}", target, codeType, code);
    }
    
    @Override
    public boolean verifyCode(String target, VerificationCodeType codeType, String code) {
        log.debug("验证验证码: target={}, codeType={}, code={}", target, codeType, code);
        
        // 查找验证码
        Optional<VerificationCode> verificationCodeOpt = verificationCodeRepository
                .findByTargetAndCodeTypeAndCodeAndUsedFalse(target, codeType, code);
        
        if (!verificationCodeOpt.isPresent()) {
            log.debug("验证码不存在或已使用: target={}, codeType={}", target, codeType);
            return false;
        }
        
        VerificationCode verificationCode = verificationCodeOpt.get();
        
        // 检查是否过期
        if (VerificationCodeUtil.isExpired(verificationCode.getExpiresAt())) {
            log.debug("验证码已过期: target={}, codeType={}", target, codeType);
            return false;
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean verifyAndUseCode(String target, VerificationCodeType codeType, String code) {
        log.debug("验证并使用验证码: target={}, codeType={}, code={}", target, codeType, code);
        
        // 检查尝试次数限制
        if (!checkAttemptFrequency(target, codeType)) {
            log.warn("验证码尝试次数过多: target={}, codeType={}", target, codeType);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS.getCode(), "验证码尝试次数过多，请稍后重试");
        }
        
        // 查找验证码
        Optional<VerificationCode> verificationCodeOpt = verificationCodeRepository
                .findByTargetAndCodeTypeAndCodeAndUsedFalse(target, codeType, code);
        
        if (!verificationCodeOpt.isPresent()) {
            log.debug("验证码不存在或已使用: target={}, codeType={}", target, codeType);
            return false;
        }
        
        VerificationCode verificationCode = verificationCodeOpt.get();
        
        // 检查是否过期
        if (VerificationCodeUtil.isExpired(verificationCode.getExpiresAt())) {
            log.debug("验证码已过期: target={}, codeType={}", target, codeType);
            return false;
        }
        
        // 标记为已使用
        verificationCodeRepository.markAsUsed(verificationCode.getId());
        
        log.debug("验证码验证成功并标记为已使用: target={}, codeType={}", target, codeType);
        return true;
    }
    
    @Override
    public boolean checkSendFrequency(String target, VerificationCodeType codeType) {
        LocalDateTime startTime = LocalDateTime.now().minusSeconds(SEND_FREQUENCY_SECONDS);
        
        long count = verificationCodeRepository.countByTargetAndCodeTypeAndCreatedAtAfter(
                target, codeType, startTime);
        
        boolean allowed = count == 0;
        log.debug("检查发送频率: target={}, codeType={}, count={}, allowed={}", 
                target, codeType, count, allowed);
        
        return allowed;
    }
    
    @Override
    public boolean checkAttemptFrequency(String target, VerificationCodeType codeType) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(ATTEMPT_FREQUENCY_MINUTES);
        
        long count = verificationCodeRepository.countByTargetAndCodeTypeAndCreatedAtAfter(
                target, codeType, startTime);
        
        boolean allowed = count < MAX_ATTEMPTS;
        log.debug("检查尝试频率: target={}, codeType={}, count={}, allowed={}", 
                target, codeType, count, allowed);
        
        return allowed;
    }
    
    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanExpiredCodes() {
        log.info("开始清理过期的验证码");
        
        LocalDateTime now = LocalDateTime.now();
        verificationCodeRepository.deleteExpiredCodes(now);
        
        log.info("清理过期的验证码完成");
    }
    
    private boolean isValidTarget(String target) {
        return VerificationCodeUtil.isValidEmail(target) || VerificationCodeUtil.isValidPhone(target);
    }
    
    private void sendCodeToTarget(String target, String code, VerificationCodeType codeType) {
        // 这里模拟发送验证码，实际项目中需要集成短信/邮件服务
        String targetType = VerificationCodeUtil.getTargetType(target);
        String codeTypeName = codeType.getDescription();
        
        if ("EMAIL".equals(targetType)) {
            log.info("发送邮件验证码: target={}, code={}, type={}", target, code, codeTypeName);
            // 实际项目中调用邮件服务发送验证码
            // emailService.sendVerificationCode(target, code, codeTypeName);
        } else if ("PHONE".equals(targetType)) {
            log.info("发送短信验证码: target={}, code={}, type={}", target, code, codeTypeName);
            // 实际项目中调用短信服务发送验证码
            // smsService.sendVerificationCode(target, code, codeTypeName);
        }
        
        // 开发环境：在日志中输出验证码，方便测试
        log.info("验证码（开发环境）: {} -> {}", target, code);
    }
}