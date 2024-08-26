package com.ex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	//패스워드 암호화 (해시)
	@Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }
	
	// 계층 권한 메소드
	@Bean
	public RoleHierarchy roleHierarchy() {

	    RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
	    // ADMIN > MAGAGER > USER 
	    // ADMIN은 MANAGER권한 접근가능
	    // MANAGER는 USER권한 접근가능
	    hierarchy.setHierarchy("ROLE_ADMIN > ROLE_MAMAGER\n" +
	            "ROLE_MAMAGER > ROLE_USER");

	    return hierarchy;
	}
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		// JWT는 session을 stateless상태로 관리 하기때문에 csrf공격 위험 x
		http
        .csrf((auth) -> auth.disable());
		//JWT 방식이라 필요 x
		http
        .formLogin((auth) -> auth.disable());
		//JWT 방식이라 필요 x
		http
        .httpBasic((auth) -> auth.disable());
		//경로별 인가 설정
		http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/","/login","/join").permitAll()
                .requestMatchers("/admin").hasRole("ADMIN")
                .anyRequest().authenticated()
        );
		
		//세션 설정
		http.sessionManagement((session) -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		return http.build();
	}
	
}
