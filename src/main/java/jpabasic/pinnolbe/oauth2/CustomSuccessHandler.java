package jpabasic.pinnolbe.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.dto.login.oauth2.CustomOAuth2User;
import jpabasic.pinnolbe.jwt.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        GrantedAuthority auth = authorities.iterator().next();
        String role = auth.getAuthority();

        // JWT 생성
        String token = jwtUtil.createJwt(username, role, 60 * 60 * 60L); // 60시간

        // ✅ Set-Cookie 수동 설정 (크로스도메인 허용)
        String cookieHeader = "Authorization=" + token +
                "; Max-Age=" + (60 * 60 * 60) +
                "; Path=/" +
                "; Domain=frontend-seolyeon.vercel.app" +
                "; HttpOnly" +
                "; Secure" +
                "; SameSite=None";

        response.setHeader("Set-Cookie", cookieHeader);

        // ✅ 로그인 후 리다이렉트
        boolean isFirstLogin = customUserDetails.isFirstLogin();
        String targetUrl = isFirstLogin
                ? "https://frontend-seolyeon.vercel.app/childInfo"
                : "https://frontend-seolyeon.vercel.app/main";

        response.sendRedirect(targetUrl);
    }
}

