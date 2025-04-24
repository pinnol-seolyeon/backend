package jpabasic.pinnolbe.config;


import jakarta.servlet.http.HttpServletRequest;
import jpabasic.pinnolbe.jwt.JwtFilter;
import jpabasic.pinnolbe.jwt.JwtUtil;
import jpabasic.pinnolbe.oauth2.CustomSuccessHandler;
import jpabasic.pinnolbe.service.login.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JwtUtil jwtUtil;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,CustomSuccessHandler customSuccessHandler,JwtUtil jwtUtil) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(corsCustomizer->corsCustomizer
                        .configurationSource(new CorsConfigurationSource(){

                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                                CorsConfiguration config = new CorsConfiguration();

                                config.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                                config.setAllowedMethods(Collections.singletonList("*")); //모든 요청 허용
                                config.setAllowCredentials(true);
                                config.setAllowedHeaders(Collections.singletonList("*")); //어떤 헤더를 받을 수 있는지
                                config.setMaxAge(3600L);

                                //
                                config.setExposedHeaders(Collections.singletonList("Set-Cookie"));
                                config.setExposedHeaders(Collections.singletonList("Authorization"));

                                return config;
                            }
                        }
                        ))

                //Form 로그인 방식 disable
                .formLogin((auth)->auth.disable())

                //HTTP Basic 인증 방식 disable
                .httpBasic((auth)->auth.disable())

                //JwtFilter 추가
                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class) //UsernamePasswordAuthenticationFilter.class 이전에 JwtFilter 등록

                //csrf disable
                .csrf(csrf -> csrf.disable())

                //oauth2
                .oauth2Login((oauth2)->oauth2
                        //소셜로그인 성공 -> 해당 사용자의 정보(userInfo)를 제공하는 endpoint로부터 사용자 정보를 받아옴
                        //-> 그 정보를 가져온 후 어떻게 처리할 지 설정 
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                               // 실제 사용자 정보를 어떻게 가공할지 정의
                                .userService(customOAuth2UserService))

                        .successHandler(customSuccessHandler))


                //세션 설정 : STATELESS
                .sessionManagement((session)->session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))


                //경로별 인가 작업
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/loginForm","/api/oauth/**").permitAll()
                        .anyRequest().authenticated()


                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config=new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(List.of("*"));

        //위의 설정들을 어떤 URL 경로에 적용할 지 지정.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;

    }
}

