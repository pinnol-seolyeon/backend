package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Controller
@RequestMapping("/api")
@Slf4j
public class LoginController {

    //로그인 테스트용
    @GetMapping("/my")
    @ResponseBody
    public String myInfo() {
        System.out.println("myroute");
        return "개힘들어";
    }

//ㅡㅛ



}
