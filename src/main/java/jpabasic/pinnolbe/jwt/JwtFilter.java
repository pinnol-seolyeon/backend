package jpabasic.pinnolbe.jwt;


import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.dto.login.oauth2.CustomOAuth2User;
import jpabasic.pinnolbe.dto.login.oauth2.UserDto;
import jpabasic.pinnolbe.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String accessToken = null;
        String refreshToken = null;

        // health-check bypass
        if (requestUri.equals("/health-check")) {
            filterChain.doFilter(request, response);
            return;
        }


        //Cookie들을 불러온 뒤 Authorization key에 담긴 쿠키를 찾음
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            filterChain.doFilter(request, response);
            return;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("Authorization")) {
                accessToken = cookie.getValue();
            } else if (cookie.getName().equals("RefreshToken")) {
                refreshToken = cookie.getValue();
            }
        }


        //Authorization 헤더 검증
        if (accessToken == null) {
            System.out.println("Authorization 쿠키 없음.. token null");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메서드 종료(필수)
            return;
        }


        try {

            //accessToken이 만료되었는지 먼저 검사
            if (jwtUtil.isExpired(accessToken)) {
                throw new ExpiredJwtException(null, null, "AccessToken 만료됨");
            }

            authenticateWithToken(accessToken);
            filterChain.doFilter(request, response);
            return;

        } catch (ExpiredJwtException e) {
            System.out.println("🚨파싱 도중 access token expired");

            //Refresh Token 검사
            if (refreshToken != null && !jwtUtil.isExpired(refreshToken)) {
                //토큰에서 username, role 획득
                String username = jwtUtil.getUsername(refreshToken);
                String role = jwtUtil.getRole(refreshToken);

                //Refresh Token DB에 저장된 것과 비교
                String savedRefreshToken = refreshTokenRepository.findByUsername(username);
                if (savedRefreshToken != null && savedRefreshToken.equals(refreshToken)) {
                    //새 Access Token 생성
                    String newAccessToken = jwtUtil.createJwt(username, role, 5 * 60 * 1000L);
                    response.addCookie(createCookie("Authorization", newAccessToken, 5 * 60));

                    //인증된 사용자로 등록
                    authenticateWithToken(newAccessToken);

                    System.out.println("✅ Access Token 재발급 성공");
                } else {
                    System.out.println("😡 Refresh Token 없음 or 만료"); ///Refresh Token 만료되면 어떡하죠?
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }


    private void authenticateWithToken(String accessToken) {
        String username=jwtUtil.getUsername(accessToken);
        String role=jwtUtil.getRole(accessToken);

        //userDto를 생성하여 값 set
        UserDto userDto=new UserDto();
        userDto.setUsername(username);
        userDto.setRole(role);

        //UserDetails(OAuth2User)에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User=new CustomOAuth2User(userDto);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken=new UsernamePasswordAuthenticationToken(customOAuth2User,null,customOAuth2User.getAuthorities());
        //세션에 사용자 등록 //SecurityContext에 인증 정보 등록->이후 컨트롤러나 @AuthenticationPrincipal 에서 접근 가능
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }


    private Cookie createCookie(String key, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        return cookie;
    }
}
