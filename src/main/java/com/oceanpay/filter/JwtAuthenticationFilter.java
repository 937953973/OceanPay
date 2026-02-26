package com.oceanpay.filter;

import com.oceanpay.exception.BusinessException;
import com.oceanpay.service.AuthService;
import com.oceanpay.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT认证过滤器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        // 如果没有Authorization头或者不是Bearer令牌，直接放行（由Spring Security处理授权）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 提取令牌
            String token = authHeader.substring(7);
            
            // 验证令牌
            if (jwtUtil.validateToken(token)) {
                // 从令牌中获取用户信息
                com.oceanpay.entity.User user = authService.validateToken(token);
                
                if (user != null) {
                    // 创建认证对象
                    UserDetails userDetails = createUserDetails(user);
                    
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置认证上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("JWT认证成功: userId={}, username={}", user.getId(), user.getUsername());
                }
            }
        } catch (BusinessException e) {
            log.warn("JWT认证失败: {}", e.getMessage());
            // 认证失败，继续过滤器链，由Spring Security处理
        } catch (Exception e) {
            log.error("JWT认证过程中发生错误: {}", e.getMessage(), e);
            // 发生异常，继续过滤器链
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 创建UserDetails对象
     */
    private UserDetails createUserDetails(com.oceanpay.entity.User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password("") // 密码不需要，因为使用JWT认证
                .authorities(authorities) // 默认角色
                .accountExpired(false)
                .accountLocked(user.getStatus() != com.oceanpay.enums.UserStatus.ACTIVE)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}