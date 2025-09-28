package com.example.getstudyroom.security.config;

import com.example.getstudyroom.security.jwt.JwtAuthenticationFilter;
import com.example.getstudyroom.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 기본 설정 (CSRF 비활성화, 세션 관리 STATELESS)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // API 경로별 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/signup").permitAll() // 로그인, 회원가입은 누구나 접근 가능
                        .requestMatchers(HttpMethod.POST, "/rooms").hasRole("ADMIN") // 방 등록은 ADMIN만
                        .requestMatchers(HttpMethod.GET, "/rooms").permitAll() // 가용성 조회는 누구나
                        .requestMatchers(HttpMethod.POST, "/reservations").hasRole("USER") // 예약 생성은 USER만
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
