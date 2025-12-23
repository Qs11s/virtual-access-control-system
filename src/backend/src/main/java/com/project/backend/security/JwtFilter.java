package com.project.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        String header = request.getHeader("Authorization");
        String username = null;
        String token = null;

        // 跳过不需要认证的路径
        if (shouldNotFilter(request)) {
            chain.doFilter(request, response);
            return;
        }

        // 提取Token
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            try {
                username = jwtUtil.extractUsername(token);
                logger.debug("提取用户名成功: {}", username);
            } catch (Exception e) {
                logger.error("提取用户名失败: {}", e.getMessage());
            }
        } else {
            logger.debug("未找到Authorization头或格式不正确");
        }

        // 验证Token并设置认证
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (jwtUtil.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("为用户 {} 设置认证信息", username);
            } else {
                logger.warn("Token验证失败，用户: {}", username);
            }
        } else if (username == null) {
            logger.debug("未提供有效Token，请求将被拒绝");
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean shouldNotFilter = path.equals("/") || 
                                 path.startsWith("/auth/") ||
                                 path.startsWith("/h2-console/") ||
                                 path.startsWith("/access/") ||
                                 path.startsWith("/verify/") ||
                                 path.startsWith("/location/");
        
        logger.debug("路径 {} 是否需要过滤: {}", path, !shouldNotFilter);
        return shouldNotFilter;
    }
}