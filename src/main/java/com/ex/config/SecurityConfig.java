package com.ex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ex.jwt.JWTFilter;
import com.ex.jwt.JWTUtil;
import com.ex.jwt.LoginFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	//AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
	private final AuthenticationConfiguration authenticationConfiguration;
	private final JWTUtil jwtUtil;
	
	
	//AuthenticationManager Bean 등록
	@Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }
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
		//JWTFilter 등록
        http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);
		
		//addFilterAt (new LoginFilter(), UsernamePasswordAuthenticationFilter.class)
		// UsernamePasswordAuthenticationFilter 자리에 LoginFilter를 대신 끼움)
		//필터 추가 LoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를 넣어야 함) 따라서 등록 필요
        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

		//세션 설정 
		//JWT를 통한 인증/인가를 위해서 세션을 STATELESS 상태로 설정하는 것이 중요하다.
		http.sessionManagement((session) -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		return http.build();
	}
	
}
