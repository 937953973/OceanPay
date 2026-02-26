# OceanPay 登录功能 API 文档

## 概述

本文档描述了 OceanPay 系统的登录和认证相关 API 接口。系统支持密码登录和验证码登录两种方式。

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: Bearer Token (JWT)
- **响应格式**: JSON

## 响应格式

所有 API 响应都遵循以下格式：

```json
{
  "code": "00000",
  "message": "成功",
  "data": {},
  "timestamp": 1678886400000
}
```

## 认证流程

### 1. 密码登录

**请求**
- **URL**: `POST /auth/login/password`
- **Content-Type**: `application/json`

**请求体**:
```json
{
  "identifier": "用户名/邮箱/手机号",
  "password": "密码",
  "loginType": "PASSWORD"
}
```

**成功响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com",
      "phone": "+8613812345678",
      "firstName": "张",
      "lastName": "三",
      "avatarUrl": "https://example.com/avatar.jpg",
      "emailVerified": true,
      "phoneVerified": false
    }
  },
  "timestamp": 1678886400000
}
```

### 2. 验证码登录

#### 2.1 发送登录验证码

**请求**
- **URL**: `POST /auth/login/send-code`
- **Content-Type**: `application/json`

**请求体**:
```json
{
  "target": "邮箱或手机号",
  "codeType": "LOGIN"
}
```

#### 2.2 使用验证码登录

**请求**
- **URL**: `POST /auth/login/verification-code`
- **Content-Type**: `application/json`

**请求体**:
```json
{
  "identifier": "邮箱或手机号",
  "loginType": "VERIFICATION_CODE",
  "verificationCode": "123456"
}
```

### 3. 用户注册

#### 3.1 发送注册验证码

**请求**
- **URL**: `POST /auth/register/send-code`
- **Content-Type**: `application/json`

**请求体**:
```json
{
  "target": "邮箱或手机号",
  "codeType": "REGISTER"
}
```

#### 3.2 验证注册验证码

**请求**
- **URL**: `POST /auth/register/verify-code`
- **Content-Type**: `application/json`

**请求体**:
```json
{
  "target": "邮箱或手机号",
  "codeType": "REGISTER",
  "code": "123456"
}
```

**成功响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": "注册验证令牌",
  "timestamp": 1678886400000
}
```

#### 3.3 注册用户

**请求**
- **URL**: `POST /auth/register`
- **Content-Type**: `application/json`

**请求体**:
```json
{
  "username": "newuser",
  "email": "newuser@example.com",
  "phone": "+8613812345678",
  "password": "Test@1234",
  "confirmPassword": "Test@1234",
  "firstName": "李",
  "lastName": "四",
  "verificationToken": "上一步获取的注册验证令牌",
  "defaultCurrency": "USD",
  "defaultLanguage": "zh"
}
```

### 4. 密码重置

#### 4.1 发送重置密码验证码

**请求**
- **URL**: `POST /auth/password/reset/send-code`
- **Content-Type**: `application/json`

**请求体**:
```json
{
  "target": "邮箱或手机号",
  "codeType": "RESET_PASSWORD"
}
```

#### 4.2 重置密码

**请求**
- **URL**: `POST /auth/password/reset`
- **Content-Type**: `application/json`

**请求体**:
```json
{
  "target": "邮箱或手机号",
  "verificationCode": "123456",
  "newPassword": "New@1234",
  "confirmNewPassword": "New@1234"
}
```

### 5. 令牌刷新

**请求**
- **URL**: `POST /auth/refresh-token`
- **Headers**: `Authorization: Bearer <refresh_token>`

### 6. 退出登录

**请求**
- **URL**: `POST /auth/logout`
- **Headers**: `Authorization: Bearer <access_token>`

## 用户管理

### 1. 获取当前用户信息

**请求**
- **URL**: `GET /user/me`
- **Headers**: `Authorization: Bearer <access_token>`

### 2. 更新用户信息

**请求**
- **URL**: `PUT /user/me`
- **Headers**: `Authorization: Bearer <access_token>`
- **Content-Type**: `application/json`

**请求体**:
```json
{
  "id": 1,
  "firstName": "王",
  "lastName": "五",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

### 3. 修改密码

**请求**
- **URL**: `POST /user/me/password`
- **Headers**: `Authorization: Bearer <access_token>`
- **Content-Type**: `application/json`

**请求体**:
```json
{
  "oldPassword": "Old@1234",
  "newPassword": "New@1234",
  "confirmNewPassword": "New@1234"
}
```

### 4. 验证邮箱

**请求**
- **URL**: `POST /user/me/email/verify`
- **Headers**: `Authorization: Bearer <access_token>`

### 5. 验证手机号

**请求**
- **URL**: `POST /user/me/phone/verify`
- **Headers**: `Authorization: Bearer <access_token>`

## 验证接口

### 1. 检查用户名是否可用

**请求**
- **URL**: `GET /user/check/username?username=desired_username`

### 2. 检查邮箱是否可用

**请求**
- **URL**: `GET /user/check/email?email=desired_email`

### 3. 检查手机号是否可用

**请求**
- **URL**: `GET /user/check/phone?phone=desired_phone`

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 00000 | 成功 |
| 99999 | 系统错误 |
| A0001 | 参数错误 |
| A0002 | 未授权 |
| A0003 | 禁止访问 |
| A0004 | 资源不存在 |
| A0005 | 资源冲突 |
| A0006 | 请求过于频繁 |
| U0001 | 用户不存在 |
| U0002 | 用户已被禁用 |
| U0003 | 用户已存在 |
| U0004 | 密码错误 |
| U0005 | 登录失败 |
| U0006 | 登录被锁定 |
| V0001 | 验证码不存在 |
| V0002 | 验证码已过期 |
| V0003 | 验证码已使用 |
| V0004 | 验证码错误 |
| V0005 | 验证码发送失败 |
| V0006 | 验证码发送过于频繁 |
| T0001 | Token已过期 |
| T0002 | Token无效 |
| T0003 | Token缺失 |
| D0001 | 地址不存在 |
| D0002 | 地址数量达到上限 |
| P0001 | 权限不足 |

## 密码要求

- 至少8个字符
- 至少包含一个大写字母
- 至少包含一个小写字母
- 至少包含一个数字
- 至少包含一个特殊字符 (!@#$%^&*()-_=+[]{}|;:,.<>?)

## 开发环境说明

1. **数据库**: H2 内存数据库
   - 控制台: http://localhost:8080/h2-console
   - JDBC URL: jdbc:h2:mem:oceanpaydb
   - 用户名: sa
   - 密码: (空)

2. **验证码**: 开发环境下验证码会直接输出到日志中，方便测试

3. **默认用户**: 可以通过注册接口创建用户，或直接插入测试数据

## 安全注意事项

1. 生产环境需要修改 JWT secret
2. 建议启用 HTTPS
3. 验证码发送频率需要根据业务需求调整
4. 密码重置令牌应有合理的过期时间
5. 建议实现登录失败次数限制和账户锁定机制