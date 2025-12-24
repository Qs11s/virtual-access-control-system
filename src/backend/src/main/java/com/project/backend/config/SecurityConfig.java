package com.project.backend.config;

import com.project.backend.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"未认证\"}");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(403);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"权限不足\"}");
                        })
                )
                // 允许H2控制台的iframe访问
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth
                        // 公开访问的路径
                        .requestMatchers(
                                "/",
                                "/auth/**",
                                "/h2-console/**"
                        ).permitAll()
                        
                        // 管理端路径 - ADMIN角色
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        
                        // 新增：选课/退课接口 - ADMIN和TEACHER角色均可访问
                        .requestMatchers(HttpMethod.POST, "/admin/courses/{courseId}/students/{studentId}").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/admin/courses/{courseId}/students/{studentId}").hasAnyRole("ADMIN", "TEACHER")
                        
                        // 教师端路径 - TEACHER角色
                        .requestMatchers("/teacher/**").hasRole("TEACHER")
                        
                        // 考勤相关路径配置
                        .requestMatchers(HttpMethod.GET, "/attendance/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/attendance/checkin").authenticated()
                        .requestMatchers(HttpMethod.GET, "/attendance/session/**").authenticated()
                        
                        // 课程相关路径配置
                        .requestMatchers(HttpMethod.GET, "/courses").authenticated()
                        .requestMatchers(HttpMethod.GET, "/courses/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/courses").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/courses/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/courses/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/courses/*/sessions").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/courses/sessions").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/courses/sessions/*").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/courses/sessions/*").hasAnyRole("TEACHER", "ADMIN")
                        
                        // 我的信息相关
                        .requestMatchers("/me/**").authenticated()
                        
                        // 门禁相关路径（核心修改：限制临时码创建为ADMIN角色，验证公开）
                        .requestMatchers(HttpMethod.POST, "/access/temp-code").hasRole("ADMIN") // 仅管理员可创建
                        .requestMatchers(HttpMethod.POST, "/access/temp-code/verify").permitAll() // 验证公开
                        .requestMatchers("/access/**").permitAll() // 其他门禁路径保持公开
                        .requestMatchers("/verify/**").permitAll()
                        .requestMatchers("/location/**").permitAll()
                        
                        // 记录相关
                        .requestMatchers("/records/**").authenticated()
                        
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}