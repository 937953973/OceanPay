# OceanPay 快速开始指南

## 项目概述

OceanPay 是一个基于 Spring Boot 的支付系统，包含完整的用户认证和登录功能。

## 系统要求

- Java 17 或更高版本
- Maven 3.6+ 或 Gradle
- IDE (推荐 IntelliJ IDEA 或 Eclipse)

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd OceanPay
```

### 2. 配置环境

项目使用 H2 内存数据库，无需额外配置。如果需要使用 MySQL，请修改 `application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/oceanpay
    username: root
    password: yourpassword
```

### 3. 构建项目

使用 Maven:

```bash
mvn clean install
```

### 4. 运行项目

使用 Maven:

```bash
mvn spring-boot:run
```

或者直接运行主类:
- 在 IDE 中运行 `OceanPayApplication.java`
- 使用命令行: `java -jar target/oceanpay-1.0.0.jar`

### 5. 验证运行状态

访问健康检查端点:
```
GET http://localhost:8080/api/health
```

预期响应:
```json
{
  "status": "UP",
  "service": "OceanPay",
  "timestamp": 1678886400000
}
```

## 测试登录功能

### 1. 注册新用户

**步骤 1: 发送注册验证码**
```
POST http://localhost:8080/api/auth/register/send-code
Content-Type: application/json

{
  "target": "user@example.com",
  "codeType": "REGISTER"
}
```

**步骤 2: 验证注册验证码**
```
POST http://localhost:8080/api/auth/register/verify-code
Content-Type: application/json

{
  "target": "user@example.com",
  "codeType": "REGISTER",
  "code": "123456"  # 查看控制台日志获取验证码
}
```

响应中包含注册令牌。

**步骤 3: 注册用户**
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "email": "user@example.com",
  "password": "Test@1234",
  "confirmPassword": "Test@1234",
  "firstName": "张",
  "lastName": "三",
  "verificationToken": "上一步获取的令牌"
}
```

### 2. 密码登录

```
POST http://localhost:8080/api/auth/login/password
Content-Type: application/json

{
  "identifier": "newuser",
  "password": "Test@1234",
  "loginType": "PASSWORD"
}
```

响应中包含访问令牌和刷新令牌。

### 3. 使用令牌访问受保护接口

```
GET http://localhost:8080/api/user/me
Authorization: Bearer <access_token>
```

## 开发环境特性

### 1. H2 数据库控制台

访问: http://localhost:8080/h2-console

连接信息:
- JDBC URL: `jdbc:h2:mem:oceanpaydb`
- 用户名: `sa`
- 密码: (空)

### 2. 验证码调试

在开发环境中，验证码会直接输出到控制台日志中:
```
验证码（开发环境）: user@example.com -> 123456
```

### 3. 默认配置

- JWT 密钥: `oceanpay-secret-key-2025-spring-boot-jwt-security`
- 令牌有效期: 1小时（访问令牌），7天（刷新令牌）
- 验证码有效期: 5分钟

## API 测试工具

### 使用 Postman

1. 导入 Postman 集合（可参考 `API_DOCUMENTATION.md`）
2. 设置环境变量:
   - `baseUrl`: `http://localhost:8080/api`
   - `accessToken`: 登录后获取的令牌

### 使用 curl

```bash
# 健康检查
curl -X GET http://localhost:8080/api/health

# 密码登录
curl -X POST http://localhost:8080/api/auth/login/password \
  -H "Content-Type: application/json" \
  -d '{"identifier":"testuser","password":"Test@1234","loginType":"PASSWORD"}'

# 获取用户信息
curl -X GET http://localhost:8080/api/user/me \
  -H "Authorization: Bearer <access_token>"
```

## 常见问题

### 1. 端口被占用

修改 `application.yml` 中的端口号:
```yaml
server:
  port: 8081
```

### 2. 数据库连接失败

检查数据库配置，确保数据库服务正在运行。

### 3. JWT 验证失败

确保使用正确的令牌格式: `Bearer <token>`

### 4. 验证码发送失败

检查日志中的错误信息，确保邮件/短信服务配置正确。

## 下一步

1. 阅读完整的 API 文档: `API_DOCUMENTATION.md`
2. 查看源代码结构
3. 运行集成测试: `mvn test`
4. 根据业务需求修改配置

## 技术支持

如有问题，请检查:
1. 应用程序日志
2. H2 数据库控制台
3. Spring Boot Actuator 端点（如果启用）