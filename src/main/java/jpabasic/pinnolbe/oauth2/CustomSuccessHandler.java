package jpabasic.pinnolbe.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.dto.login.oauth2.CustomOAuth2User;
import jpabasic.pinnolbe.jwt.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    public CustomSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        System.out.println("⭐ CustomSuccessHandler 시작");
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        System.out.println("⭐ customUserDetails = " + customUserDetails);
        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        GrantedAuthority auth = authorities.iterator().next();
        String role = auth.getAuthority();

        // JWT 생성
        String token = jwtUtil.createJwt(username, role, 60 * 60 * 60L); // 60시간
        System.out.println("⭐token="+token);

        ResponseCookie cookie = ResponseCookie.from("Authorization", token)
                .httpOnly(true)
                .secure(true) 
                .path("/")
                .sameSite("None") //크로스 도메인 요청에도 쿠키 전송 허용
                .domain("finnol.site") //쿠키가 적용될 도메인
                .maxAge(Duration.ofDays(1))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // ✅ 로그인 후 리다이렉트
        boolean isFirstLogin = customUserDetails.isFirstLogin();
        String targetUrl = isFirstLogin
                ? "https://frontend-seolyeon.vercel.app/childInfo"
                : "https://frontend-seolyeon.vercel.app/main";

//        logger.debug("🚨"+cookieHeader);
        response.sendRedirect(targetUrl);
//        String callbackUrl="https://frontend-seolyeon.vercel.app/callback";
//        response.sendRedirect(callbackUrl);
    }
}

