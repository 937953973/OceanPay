-- OceanPay 数据库初始化脚本
-- 创建测试用户数据

-- 清空现有数据（谨慎使用）
-- DELETE FROM user_login_log;
-- DELETE FROM user_address;
-- DELETE FROM user_preference;
-- DELETE FROM verification_code;
-- DELETE FROM user;

-- 插入测试用户
INSERT INTO users (id, username, email, phone, password_hash, first_name, last_name, status, email_verified, phone_verified, created_at, updated_at) VALUES
(1, 'admin', 'admin@oceanpay.com', '+8613812345678', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwE.', '系统', '管理员', 1, true, true, NOW(), NOW()),
(2, 'testuser', 'test@example.com', '+8613812345679', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwE.', '测试', '用户', 1, true, false, NOW(), NOW()),
(3, 'john_doe', 'john.doe@example.com', '+8613812345680', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwE.', 'John', 'Doe', 1, true, true, NOW(), NOW());

-- 注意：密码都是 'Test@1234' 的 BCrypt 哈希值

-- 插入用户偏好设置
INSERT INTO user_preference (id, user_id, default_currency, default_language, timezone, notification_email, notification_sms, notification_push, marketing_consent, created_at, updated_at) VALUES
(1, 1, 'USD', 'en', 'UTC', true, false, true, false, NOW(), NOW()),
(2, 2, 'CNY', 'zh', 'Asia/Shanghai', true, true, true, true, NOW(), NOW()),
(3, 3, 'EUR', 'en', 'Europe/London', true, false, true, false, NOW(), NOW());

-- 插入用户地址
INSERT INTO user_address (id, user_id, address_type, is_default, recipient_name, phone, country_code, state, city, address_line1, address_line2, postal_code, created_at, updated_at) VALUES
(1, 1, 1, true, '系统管理员', '+8613812345678', 'CN', '北京', '北京市', '朝阳区建国路88号', 'SOHO现代城A座', '100022', NOW(), NOW()),
(2, 2, 1, true, '测试用户', '+8613812345679', 'CN', '上海', '上海市', '浦东新区陆家嘴环路', '金茂大厦', '200120', NOW(), NOW()),
(3, 2, 2, true, '测试用户', '+8613812345679', 'CN', '上海', '上海市', '浦东新区陆家嘴环路', '金茂大厦', '200120', NOW(), NOW()),
(4, 3, 1, true, 'John Doe', '+8613812345680', 'US', 'California', 'San Francisco', '123 Main Street', 'Suite 100', '94105', NOW(), NOW());

-- 插入登录日志（示例数据）
INSERT INTO user_login_log (id, user_id, login_type, ip_address, user_agent, login_status, failure_reason, created_at) VALUES
(1, 1, 1, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 1, NULL, DATEADD('HOUR', -2, NOW())),
(2, 2, 1, '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 1, NULL, DATEADD('HOUR', -1, NOW())),
(3, 2, 1, '192.168.1.102', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/537.36', 2, '密码错误', DATEADD('MINUTE', -30, NOW())),
(4, 3, 2, '192.168.1.103', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 1, NULL, DATEADD('MINUTE', -15, NOW()));

-- 更新序列（如果需要）
-- ALTER SEQUENCE user_seq RESTART WITH 100;
-- ALTER SEQUENCE user_preference_seq RESTART WITH 100;
-- ALTER SEQUENCE user_address_seq RESTART WITH 100;
-- ALTER SEQUENCE user_login_log_seq RESTART WITH 100;