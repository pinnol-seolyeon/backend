package jpabasic.pinnolbe.config;

import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.jwt.JwtFilter;
import jpabasic.pinnolbe.jwt.JwtUtil;
import jpabasic.pinnolbe.jwt.TokenService;
import jpabasic.pinnolbe.oauth2.CustomSuccessHandler;
import jpabasic.pinnolbe.repository.RefreshTokenRepository;
import jpabasic.pinnolbe.service.login.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          CustomSuccessHandler customSuccessHandler,
                          JwtUtil jwtUtil,
                          TokenService tokenService) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   RefreshTokenRepository refreshTokenRepository) throws Exception {

        http
                // ✅ CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ Form 로그인, Basic 로그인 비활성화
                .formLogin(auth -> auth.disable())
                .httpBasic(auth -> auth.disable())

                // ✅ JWT 필터 추가
                .addFilterBefore(new JwtFilter(jwtUtil, tokenService),
                        UsernamePasswordAuthenticationFilter.class)

                // ✅ CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // ✅ 인증되지 않은 요청 시 401 반환
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                )

                // ✅ 세션 관리 (Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ✅ OAuth2 로그인
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(endpoint ->
                                endpoint.userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                )

                // ✅ 경로별 인가 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/loginForm",
                                "/api/oauth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/health-check",

                                // --- 👇 SSE 및 비동기 처리용 API ---
                                "/api/question/stream",
                                "/api/session/commit",
                                "/api/payment/success",
                                "/api/payment/fail",
                                "/payment/**"
                        ).permitAll()

                        // --- 👇 그 외는 인증 필요 ---
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://3.38.74.5:3000",
                "https://finnol.co.kr",
                "https://www.finnol.co.kr",
                "https://api.finnol.co.kr"
        ));
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"
        ));
        config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
