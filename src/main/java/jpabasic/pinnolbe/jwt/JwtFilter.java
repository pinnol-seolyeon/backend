package jpabasic.pinnolbe.jwt;


import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.domain.RefreshToken;
import jpabasic.pinnolbe.dto.login.oauth2.CustomOAuth2User;
import jpabasic.pinnolbe.dto.login.oauth2.UserDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();


        //Swagger,API Docs 요청은 필터에서 그냥 통과
        if(requestUri.startsWith("/swagger-ui")
                ||requestUri.startsWith("/v3/api-docs")
                ||requestUri.startsWith("/swagger-resources")){
            filterChain.doFilter(request,response);
            return;
        }

        // health-check bypass
        if (requestUri.equals("/health-check")) {
            filterChain.doFilter(request, response);
            return;
        }


        String accessToken=getTokenFromCookies(request,"Authorization");
        System.out.println("🍪 기존 accessToken:"+accessToken);
        String refreshToken=getTokenFromCookies(request,"RefreshToken");
        System.out.println("🍪 기존 refreshToken:"+refreshToken);

        try{
            if(accessToken!=null && !jwtUtil.isExpired(accessToken)){
                System.out.println("✏️access token 정상");
                //access token 정상
                authenticateWithToken(accessToken);
                log.info("😎 access token 유효 -> security context 저장 완료");
            }else if(refreshToken!=null && !jwtUtil.isExpired(refreshToken)){ //refreshToken 존재 && 만료 X
                //access token 만료 -> refresh token으로 새로 발급
                System.out.println("✏️ access token 만료 -> refresh token으로 access token 새로 발급 ");
                String newAccessToken=tokenService.reissueAccessToken(refreshToken,response);
                authenticateWithToken(newAccessToken);
                log.info("😎 access token 재발급 및 securityContext 저장 완료");
            }else {
                // Refresh Token도 없거나 만료 → 쿠키 삭제 후 재로그인 유도
                clearAuthCookies(response);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Refresh token expired. Please login again.");
                return;
            }
            }catch(CustomException e){
                log.warn("인증 실패:{}",e.getErrorCode());
                return;
            }
        filterChain.doFilter(request,response);
        }



    private void authenticateWithToken(String accessToken) {
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        //userDto를 생성하여 값 set
        UserDto userDto = new UserDto(username,role);

        //UserDetails(OAuth2User)에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDto);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        //세션에 사용자 등록 //SecurityContext에 인증 정보 등록->이후 컨트롤러나 @AuthenticationPrincipal 에서 접근 가능
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void clearAuthCookies(HttpServletResponse response) {
        Cookie expiredAuth = new Cookie("Authorization", null);
        expiredAuth.setPath("/");
        expiredAuth.setMaxAge(0);

        Cookie expiredRefresh = new Cookie("RefreshToken", null);
        expiredRefresh.setPath("/");
        expiredRefresh.setMaxAge(0);

        response.addCookie(expiredAuth);
        response.addCookie(expiredRefresh);
    }


    private String getTokenFromCookies(HttpServletRequest request,String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }


}
