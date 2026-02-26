# OceanPay API 设计文档

## 用户认证模块 API

### 1. 注册相关 API

#### 1.1 发送注册验证码
- **URL**: `/api/v1/auth/register/send-code`
- **Method**: POST
- **Description**: 发送注册验证码到邮箱或手机
- **Request Body**:
```json
{
  "target": "user@example.com", 
  "targetType": "EMAIL" 
}
```
- **Response**:
```json
{
  "success": true,
  "message": "验证码已发送",
  "data": null
}
```

#### 1.2 验证注册验证码
- **URL**: `/api/v1/auth/register/verify-code`
- **Method**: POST
- **Description**: 验证注册验证码
- **Request Body**:
```json
{
  "target": "user@example.com",
  "code": "123456"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "验证码验证成功",
  "data": {
    "token": "verification_token_here"
  }
}
```

#### 1.3 用户注册
- **URL**: `/api/v1/auth/register`
- **Method**: POST
- **Description**: 用户注册
- **Request Body**:
```json
{
  "username": "john_doe",
  "email": "user@example.com",
  "phone": "+8612345678901",
  "password": "Password123!",
  "firstName": "John",
  "lastName": "Doe",
  "verificationToken": "token_from_verify_code",
  "defaultCurrency": "USD",
  "defaultLanguage": "en"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "user@example.com",
    "accessToken": "jwt_token_here",
    "refreshToken": "refresh_token_here"
  }
}
```

### 2. 登录相关 API

#### 2.1 密码登录
- **URL**: `/api/v1/auth/login/password`
- **Method**: POST
- **Description**: 使用密码登录
- **Request Body**:
```json
{
  "username": "john_doe", 
  "password": "Password123!"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "user@example.com",
    "accessToken": "jwt_token_here",
    "refreshToken": "refresh_token_here",
    "expiresIn": 3600
  }
}
```

#### 2.2 发送登录验证码
- **URL**: `/api/v1/auth/login/send-code`
- **Method**: POST
- **Description**: 发送登录验证码
- **Request Body**:
```json
{
  "target": "user@example.com"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "验证码已发送",
  "data": null
}
```

#### 2.3 验证码登录
- **URL**: `/api/v1/auth/login/code`
- **Method**: POST
- **Description**: 使用验证码登录
- **Request Body**:
```json
{
  "target": "user@example.com",
  "code": "123456"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "user@example.com",
    "accessToken": "jwt_token_here",
    "refreshToken": "refresh_token_here",
    "expiresIn": 3600
  }
}
```

#### 2.4 刷新 Token
- **URL**: `/api/v1/auth/refresh`
- **Method**: POST
- **Description**: 刷新访问令牌
- **Request Body**:
```json
{
  "refreshToken": "refresh_token_here"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "Token刷新成功",
  "data": {
    "accessToken": "new_jwt_token_here",
    "refreshToken": "new_refresh_token_here",
    "expiresIn": 3600
  }
}
```

#### 2.5 退出登录
- **URL**: `/api/v1/auth/logout`
- **Method**: POST
- **Description**: 退出登录
- **Headers**: `Authorization: Bearer {token}`
- **Response**:
```json
{
  "success": true,
  "message": "退出成功",
  "data": null
}
```

### 3. 密码管理 API

#### 3.1 发送重置密码验证码
- **URL**: `/api/v1/auth/password/reset/send-code`
- **Method**: POST
- **Description**: 发送重置密码验证码
- **Request Body**:
```json
{
  "target": "user@example.com"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "验证码已发送",
  "data": null
}
```

#### 3.2 重置密码
- **URL**: `/api/v1/auth/password/reset`
- **Method**: POST
- **Description**: 重置密码
- **Request Body**:
```json
{
  "target": "user@example.com",
  "code": "123456",
  "newPassword": "NewPassword123!"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "密码重置成功",
  "data": null
}
```

#### 3.3 修改密码
- **URL**: `/api/v1/auth/password/change`
- **Method**: POST
- **Description**: 修改密码（需要登录）
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:
```json
{
  "oldPassword": "OldPassword123!",
  "newPassword": "NewPassword123!"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "密码修改成功",
  "data": null
}
```

### 4. 用户信息 API

#### 4.1 获取当前用户信息
- **URL**: `/api/v1/users/me`
- **Method**: GET
- **Description**: 获取当前登录用户信息
- **Headers**: `Authorization: Bearer {token}`
- **Response**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "user@example.com",
    "phone": "+8612345678901",
    "firstName": "John",
    "lastName": "Doe",
    "avatarUrl": "https://example.com/avatar.jpg",
    "status": "ACTIVE",
    "emailVerified": true,
    "phoneVerified": true,
    "defaultCurrency": "USD",
    "defaultLanguage": "en",
    "timezone": "UTC",
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

#### 4.2 更新用户信息
- **URL**: `/api/v1/users/me`
- **Method**: PUT
- **Description**: 更新当前用户信息
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "更新成功",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "avatarUrl": "https://example.com/new-avatar.jpg"
  }
}
```

#### 4.3 更新用户偏好设置
- **URL**: `/api/v1/users/me/preferences`
- **Method**: PUT
- **Description**: 更新用户偏好设置
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:
```json
{
  "defaultCurrency": "EUR",
  "defaultLanguage": "fr",
  "timezone": "Europe/Paris",
  "notificationEmail": true,
  "notificationSms": false,
  "notificationPush": true,
  "marketingConsent": true
}
```
- **Response**:
```json
{
  "success": true,
  "message": "偏好设置更新成功",
  "data": {
    "defaultCurrency": "EUR",
    "defaultLanguage": "fr",
    "timezone": "Europe/Paris"
  }
}
```

### 5. 地址管理 API

#### 5.1 获取地址列表
- **URL**: `/api/v1/users/me/addresses`
- **Method**: GET
- **Description**: 获取当前用户的地址列表
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `addressType`: 地址类型（可选，1-收货地址，2-发票地址）
- **Response**:
```json
{
  "success": true,
  "message": "获取成功",
  "data": [
    {
      "addressId": 1,
      "addressType": "SHIPPING",
      "isDefault": true,
      "recipientName": "John Doe",
      "phone": "+8612345678901",
      "countryCode": "US",
      "state": "California",
      "city": "San Francisco",
      "addressLine1": "123 Main St",
      "addressLine2": "Apt 4B",
      "postalCode": "94105",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### 5.2 添加地址
- **URL**: `/api/v1/users/me/addresses`
- **Method**: POST
- **Description**: 添加新地址
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:
```json
{
  "addressType": "SHIPPING",
  "isDefault": true,
  "recipientName": "John Doe",
  "phone": "+8612345678901",
  "countryCode": "US",
  "state": "California",
  "city": "San Francisco",
  "addressLine1": "123 Main St",
  "addressLine2": "Apt 4B",
  "postalCode": "94105"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "地址添加成功",
  "data": {
    "addressId": 1,
    "addressType": "SHIPPING",
    "isDefault": true,
    "recipientName": "John Doe",
    "phone": "+8612345678901",
    "countryCode": "US",
    "state": "California",
    "city": "San Francisco",
    "addressLine1": "123 Main St",
    "addressLine2": "Apt 4B",
    "postalCode": "94105",
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

#### 5.3 更新地址
- **URL**: `/api/v1/users/me/addresses/{addressId}`
- **Method**: PUT
- **Description**: 更新地址
- **Headers**: `Authorization: Bearer {token}`
- **Path Parameters**:
  - `addressId`: 地址ID
- **Request Body**: 同添加地址
- **Response**: 同添加地址

#### 5.4 删除地址
- **URL**: `/api/v1/users/me/addresses/{addressId}`
- **Method**: DELETE
- **Description**: 删除地址
- **Headers**: `Authorization: Bearer {token}`
- **Path Parameters**:
  - `addressId`: 地址ID
- **Response**:
```json
{
  "success": true,
  "message": "地址删除成功",
  "data": null
}
```

#### 5.5 设置默认地址
- **URL**: `/api/v1/users/me/addresses/{addressId}/default`
- **Method**: PUT
- **Description**: 设置默认地址
- **Headers**: `Authorization: Bearer {token}`
- **Path Parameters**:
  - `addressId`: 地址ID
- **Response**:
```json
{
  "success": true,
  "message": "默认地址设置成功",
  "data": null
}
```

## 状态码说明

- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权
- `403`: 禁止访问
- `404`: 资源不存在
- `409`: 资源冲突（如用户名已存在）
- `429`: 请求过于频繁
- `500`: 服务器内部错误

## 安全要求

1. 所有敏感操作（注册、登录、修改密码等）需要验证码或密码验证
2. 密码必须加密存储（使用BCrypt）
3. JWT Token有效期：Access Token 1小时，Refresh Token 7天
4. 验证码有效期：5分钟
5. 验证码尝试次数限制：5次
6. 登录失败次数限制：5次，超过后锁定15分钟