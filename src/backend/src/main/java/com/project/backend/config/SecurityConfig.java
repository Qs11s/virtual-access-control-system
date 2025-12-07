package com.project.backend.config;

import com.project.backend.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/auth/**",
                                "/h2-console/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/attendance/checkin",
                                "/schedule/me",
                                "/attendance/me"
                        ).hasRole("STUDENT")
                        .requestMatchers(
                                "/teacher/**",
                                "/attendance/session/*/summary"
                        ).hasRole("TEACHER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/courses").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/courses").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/courses/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/courses/**").hasRole("ADMIN")
                        .requestMatchers("/courses/{id}/sessions").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers("/courses/sessions/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(
                                "/location/**",
                                "/access/**",
                                "/attendance/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}