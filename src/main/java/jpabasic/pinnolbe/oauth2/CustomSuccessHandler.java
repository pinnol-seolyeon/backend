package jpabasic.pinnolbe.oauth2;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.dto.login.oauth2.CustomOAuth2User;
import jpabasic.pinnolbe.jwt.JwtUtil;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.beans.factory.annotation.Value;
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
    private final UserService userService;

    @Value("${custom.frontend.deploy.url}")
    private String deployUrl;

    @Value("${custom.frontend.local.url}")
    private String localUrl;

    public CustomSuccessHandler(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil=jwtUtil;
        this.userService=userService;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {



        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        //JWT 생성 위해 필요한 값들을 불러옴
        String username=customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities=authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator=authorities.iterator();
        GrantedAuthority auth=iterator.next();
        String role=auth.getAuthority();

        //JWT 생성
        String token=jwtUtil.createJwt(username,role,60*60*1000L);
//        //JWT 생성
//        String token=jwtUtil.createJwt(username,role,60*60*1000L);

        //Access Token 생성 : 5분
        String accessToken=jwtUtil.createJwt(username,role,5*60*1000L);

        //Refresh Token : 14일
        String refreshToken=jwtUtil.createJwt(username,role,7*24*60*60*1000L);
        userService.saveRefreshToken(username,refreshToken);

        //토큰은 쿠키 방식으로 프론트 측에 전달 -> 리다이렉트
        //access Token
        response.addCookie(createCookie("Authorization",accessToken,300));//5분

        //refresh Token
        response.addCookie(createCookie("RefreshToken",refreshToken,14 * 24 * 60 * 60));//14일

        //첫 로그인 -> 자녀 정보 받기, n번째 로그인 -> 자녀 정보 안받아도됨
        boolean isFirstLogin=customUserDetails.isFirstLogin();
        String targetUrl=isFirstLogin? deployUrl+"/childInfo":deployUrl+"/main";

        response.sendRedirect(targetUrl);

    }

    private Cookie createCookie(String key, String value,int maxAgeSeconds) {

        Cookie cookie=new Cookie(key,value);
        cookie.setMaxAge(60*60*1000);

        //cookie.setSecure(true);
        cookie.setPath("/"); //모든 위치(전역)에서 쿠키를 볼 수 있음
        cookie.setHttpOnly(true); //JavaScript가 쿠키를 가져갈 수 없도록

        return cookie;
    }

}