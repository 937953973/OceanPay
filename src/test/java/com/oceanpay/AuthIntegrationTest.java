package com.oceanpay;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanpay.dto.LoginRequest;
import com.oceanpay.dto.LoginResponse;
import com.oceanpay.dto.RegisterRequest;
import com.oceanpay.dto.SendVerificationCodeRequest;
import com.oceanpay.entity.User;
import com.oceanpay.enums.UserStatus;
import com.oceanpay.repository.UserRepository;
import com.oceanpay.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash(PasswordUtil.encode("Test@1234"))
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .build();
        
        userRepository.save(testUser);
    }
    
    @Test
    void testPasswordLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("testuser");
        request.setPassword("Test@1234");
        request.setLoginType("PASSWORD");
        
        mockMvc.perform(post("/api/auth/login/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.username").value("testuser"));
    }
    
    @Test
    void testPasswordLogin_WrongPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("testuser");
        request.setPassword("WrongPassword");
        request.setLoginType("PASSWORD");
        
        mockMvc.perform(post("/api/auth/login/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("U0004"))
                .andExpect(jsonPath("$.message").value("密码错误"));
    }
    
    @Test
    void testPasswordLogin_UserNotFound() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("nonexistent");
        request.setPassword("Test@1234");
        request.setLoginType("PASSWORD");
        
        mockMvc.perform(post("/api/auth/login/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("U0001"))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }
    
    @Test
    void testSendRegisterVerificationCode() throws Exception {
        SendVerificationCodeRequest request = new SendVerificationCodeRequest();
        request.setTarget("newuser@example.com");
        request.setCodeType("REGISTER");
        
        mockMvc.perform(post("/api/auth/register/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));
    }
    
    @Test
    void testSendRegisterVerificationCode_EmailAlreadyRegistered() throws Exception {
        SendVerificationCodeRequest request = new SendVerificationCodeRequest();
        request.setTarget("test@example.com"); // 已注册的邮箱
        request.setCodeType("REGISTER");
        
        mockMvc.perform(post("/api/auth/register/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("U0003"))
                .andExpect(jsonPath("$.message").value("邮箱已被注册"));
    }
    
    @Test
    void testGetCurrentUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testGetCurrentUser_Authorized() throws Exception {
        // 首先登录获取令牌
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier("testuser");
        loginRequest.setPassword("Test@1234");
        loginRequest.setLoginType("PASSWORD");
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        String response = loginResult.getResponse().getContentAsString();
        
        // 使用 TypeReference 来正确解析泛型类型
        com.oceanpay.dto.ApiResponse<LoginResponse> apiResponse = objectMapper.readValue(
                response,
                new TypeReference<com.oceanpay.dto.ApiResponse<LoginResponse>>() {}
        );

        LoginResponse loginResponse = apiResponse.getData();
        String accessToken = loginResponse.getAccessToken();
        
        // 使用令牌访问受保护端点
        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }
}