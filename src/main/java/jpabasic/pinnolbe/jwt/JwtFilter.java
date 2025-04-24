package jpabasic.pinnolbe.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.dto.login.oauth2.CustomOAuth2User;
import jpabasic.pinnolbe.dto.login.oauth2.UserDto;
import org.apache.catalina.UserDatabase;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //Cookie들을 불러온 뒤 Authorization key에 담긴 쿠키를 찾음
        String authorization=null;
        Cookie[] cookies = request.getCookies(); //쿠키 리스트에 담기
        for(Cookie cookie:cookies){

            System.out.println(cookie.getName());
            if(cookie.getName().equals("Authorization")){
                authorization=cookie.getValue();
            }
        }


        //Authorization 헤더 검증
        if(authorization==null){
            System.out.println("token null");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메서드 종료(필수)
            return;
        }

        //토큰
        String token=authorization;

        //토큰 소멸 시간 검증
        if(jwtUtil.isExpired(token)){ //isExpired 메서드를 통해 검증
            System.out.println("token expired");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메서드 종료(필수)
            return;
        }

        //토큰에서 username, role 획득
        String username=jwtUtil.getUsername(token);
        String role=jwtUtil.getRole(token);

        //userDto를 생성하여 값 set
        UserDto userDto=new UserDto();
        userDto.setUsername(username);
        userDto.setRole(role);

        //UserDetails에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User=new CustomOAuth2User(userDto);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken=new UsernamePasswordAuthenticationToken(customOAuth2User,null,customOAuth2User.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);

    }
}
