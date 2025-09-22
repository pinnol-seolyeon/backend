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
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
//        String accessToken = null;
//        String refreshToken = null;


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





        //Cookie들을 불러온 뒤 Authorization key에 담긴 쿠키를 찾음
//        Cookie[] cookies = request.getCookies();
//
//        if (cookies == null) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        for (Cookie cookie : cookies) {
//            if (cookie.getName().equals("Authorization")) {
//                accessToken = cookie.getValue();
//            } else if (cookie.getName().equals("RefreshToken")) {
//                refreshToken = cookie.getValue();
//            }
//        }
        String accessToken=getTokenFromCookies(request,"Authorization");
        String refreshToken=getTokenFromCookies(request,"RefreshToken");

        try {
            //Authorization 헤더 검증
//            validateTokens(accessToken, refreshToken);
            System.out.println("✅ validaToken 완료");
            authenticateWithToken(accessToken);

        } catch (CustomException e) {
            ErrorCode errorCode = e.getErrorCode();

            if (errorCode == ErrorCode.REISSUE_TOKEN) {
                try {
                    String newAccessToken = tokenService.reissueAccessToken(refreshToken, response);
                    log.info("🍪 access token 재발급 완료");

                    authenticateWithToken(newAccessToken);
                    log.info("🍪 access token 유저 정보 저장 완료");
                } catch (CustomException reissueEx) {
                    return;
                }
            } else {
                return;
            }

        }
        filterChain.doFilter(request, response);
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



    public void validateTokens(String accessToken, String refreshToken) {

        if (accessToken == null && refreshToken == null) {
            throw new CustomException(ErrorCode.NO_COOKIE);
        }

        if (accessToken == null) {
            throw new CustomException(ErrorCode.REISSUE_TOKEN);
        }

        if (jwtUtil.isExpired(accessToken)){
            throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        }

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
