package com.vueadmin.config.security;

import com.alibaba.fastjson2.JSON;
import com.vueadmin.dto.ApiResponse;
import com.vueadmin.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_NAME = "Authorization";
    private static final String REDIS_TOKEN_PREFIX = "mdm:token:";
    private static final String REDIS_USER_PERMISSIONS_PREFIX = "mdm:user:permissions:";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 检查Token是否在Redis中有效（未登出）
            String account = jwtTokenProvider.getAccountFromToken(token);

            // 尝试从Redis获取token，如果Redis不可用则跳过验证
            try {
                String redisToken = redisTemplate.opsForValue().get(REDIS_TOKEN_PREFIX + account);
                if (token.equals(redisToken)) {
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    String name = jwtTokenProvider.getNameFromToken(token);

                    // 从Redis获取权限列表
                    String permissionsKey = REDIS_USER_PERMISSIONS_PREFIX + userId;
                    String permissionsJson = redisTemplate.opsForValue().get(permissionsKey);

                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    StringUtils.hasText(permissionsJson)
                                            ? JSON.parseArray(permissionsJson, String.class).stream()
                                                    .map(SimpleGrantedAuthority::new)
                                                    .toList()
                                            : Collections.emptyList()
                            );

                    // 设置额外信息
                    authentication.setDetails(new UserPrincipal(userId, account, name));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Redis不可用时，直接使用token验证
                log.warn("Redis连接失败，使用token直接验证: {}", e.getMessage());
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                String name = jwtTokenProvider.getNameFromToken(token);

                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.emptyList()
                        );

                // 设置额外信息
                authentication.setDetails(new UserPrincipal(userId, account, name));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中解析Token
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 将Token存入Redis
     */
    public void storeToken(String account, String token) {
        try {
            redisTemplate.opsForValue().set(
                    REDIS_TOKEN_PREFIX + account,
                    token,
                    24, TimeUnit.HOURS
            );
        } catch (Exception e) {
            log.warn("Redis不可用，跳过token存储: {}", e.getMessage());
        }
    }

    /**
     * 移除Token（登出）
     */
    public void removeToken(String account) {
        try {
            redisTemplate.delete(REDIS_TOKEN_PREFIX + account);
        } catch (Exception e) {
            log.warn("Redis不可用，跳过token删除: {}", e.getMessage());
        }
    }

    /**
     * 用户主体信息
     */
    public record UserPrincipal(Long userId, String account, String name) {}
}
