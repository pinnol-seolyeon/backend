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

        String requestUri=request.getRequestURI();

        // health-check bypass
        if (requestUri.equals("/health-check")){
            filterChain.doFilter(request, response);
            return;
        }


        //Cookie들을 불러온 뒤 Authorization key에 담긴 쿠키를 찾음
        String authorization=null;
        Cookie[] cookies = request.getCookies(); //쿠키 리스트에 담기

        if(cookies==null || cookies.length==0){
            System.out.println("🍪🍪No cookies found");
            filterChain.doFilter(request, response); //여기서 종료하지 않으면 NPE 발생
            return;
        }

        for(Cookie cookie:cookies){

            System.out.println("🥸Cookie:"+cookie.getName());
            //쿠키에서 Authorization JWT 토큰을 꺼냄
            if(cookie.getName().equals("Authorization")){
                authorization=cookie.getValue();
            }
        }


        //Authorization 헤더 검증
        if(authorization==null){
            System.out.println("Authorization 쿠키 없음.. token null");
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
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().write("Expired JWT.Please login again.");

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
        //세션에 사용자 등록 //SecurityContext에 인증 정보 등록->이후 컨트롤러나 @AuthenticationPrincipal 에서 접근 가능 
        SecurityContextHolder.getContext().setAuthentication(authToken);

        System.out.println("🙆 JwtFilter 성공");

        filterChain.doFilter(request, response);

    }
}
