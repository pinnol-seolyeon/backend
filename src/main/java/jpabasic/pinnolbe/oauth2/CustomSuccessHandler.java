package jpabasic.pinnolbe.oauth2;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.domain.RefreshToken;
import jpabasic.pinnolbe.dto.login.oauth2.CustomOAuth2User;
import jpabasic.pinnolbe.jwt.JwtUtil;
import jpabasic.pinnolbe.repository.RefreshTokenRepository;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${custom.frontend.deploy.url}")
    private String deployUrl;

    @Value("${custom.frontend.local.url}")
    private String localUrl;

    public CustomSuccessHandler(JwtUtil jwtUtil, UserService userService, RefreshTokenRepository refreshTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {


        //OAuth2User //사용자 정보 가져옴
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        //JWT 생성 위해 필요한 값들을 불러옴
        String username = customUserDetails.getUsername();

        //Spring Security의 인증 객체에서 role 추출
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();


        //Access Token 생성 : 5분
        String accessToken = jwtUtil.createJwt(username, role, 5 * 60 * 1000L);

        //refresh token 재사용 or 생성
        String refreshToken;
        Optional<RefreshToken> token = refreshTokenRepository.findByUsername(username);
        if (token.isPresent()) {
            String existingToken = token.get().getToken();

            if (!jwtUtil.isExpired(existingToken)) {
                refreshToken = existingToken;
                userService.saveExistingRefreshToken(username, refreshToken);
            }else {
                    // ❌ 만료 → 새로 발급
                    userService.deleteExpiredRefreshToken(token.get());
                    refreshToken = jwtUtil.createJwt(username, role, 14 * 24 * 60 * 60 * 1000L);
                    userService.saveNewRefreshToken(username, refreshToken);
                }
            } else {
                // ❌ 아예 없으면 → 새로 발급
                refreshToken = jwtUtil.createJwt(username, role, 14 * 24 * 60 * 60 * 1000L);
                userService.saveNewRefreshToken(username, refreshToken);
            }


        //토큰은 쿠키 방식으로 프론트 측에 전달 -> 리다이렉트
        //access Token
        response.addCookie(createCookie("Authorization", accessToken, 10 * 60));//5분

        //refresh Token
        response.addCookie(createCookie("RefreshToken", refreshToken, 14 * 24 * 60 * 60));//14일

        //첫 로그인 -> 자녀 정보 받기, n번째 로그인 -> 자녀 정보 안받아도됨
        boolean isFirstLogin = customUserDetails.isFirstLogin();
        String targetUrl = isFirstLogin ? deployUrl + "/childInfo" : deployUrl + "/main";

        response.sendRedirect(targetUrl);

    }

    private Cookie createCookie(String key, String value, int maxAgeSeconds) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setSecure(true); //https 환경에서만 쿠키 전송
        cookie.setPath("/"); //모든 위치(전역)에서 쿠키를 볼 수 있음
        cookie.setHttpOnly(true); //JavaScript가 쿠키를 가져갈 수 없도록

        return cookie;
    }

}