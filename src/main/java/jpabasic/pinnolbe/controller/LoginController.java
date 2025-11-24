package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.jwt.JwtUtil;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class LoginController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public LoginController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/logout")
    @Operation(summary="로그아웃")
    public ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {
        //refresh Token 추출
        String refreshToken=jwtUtil.getTokenFromCookie(request,"RefreshToken");
        System.out.println("✔️ cookie에서 추출된 refreshToken:"+refreshToken);

        userService.logout(request,response,refreshToken);
        return ApiResponse.success("로그아웃이 완료되었어요.",null);
    }
}
