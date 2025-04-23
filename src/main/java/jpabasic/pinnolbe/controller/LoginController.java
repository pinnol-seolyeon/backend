package jpabasic.pinnolbe.controller;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.login.KakaoUserDto;
import jpabasic.pinnolbe.service.login.KaKaoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api")
@Slf4j
public class LoginController {

    private final KaKaoService kaKaoService;

    public LoginController(KaKaoService kaKaoService) {
        this.kaKaoService = kaKaoService;
    }

    @GetMapping("/loginForm")
    public String loginForm() {
        return "loginForm";
    }

//    @GetMapping("/callback")
//    public ResponseEntity<?> callback(@RequestParam("code") String code) throws IOException {
//        System.out.println("🌲"+code);
//        String accessToken=kaKaoService.getAccessTokenFromKakao(code);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }

    /// code -> access token & user정보 받아오기 -> jwt 발급 (프론트에 전달)
    @PostMapping("/oauth/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String,String> body) {
        String code = null;
        try {
            code = body.get("code");
        } catch (Exception e) {
            log.error("🤨" + e.getMessage());
        }


        /// access token 받아오기
        String accessToken = kaKaoService.getAccessTokenFromKakao(code);
        log.info("✅accessToken:{}", accessToken);

        /// 사용자 정보 가져오기
        KakaoUserDto userInfo=kaKaoService.getUserInfoFromKakao(accessToken);
        log.info("✅userInfo:{}", userInfo);

        /// 로그인 + 회원 가입
        // 서비스에 가입되어 있는지 확인 -> 있으면 로그인, 없으면 회원가입
        User user=kaKaoService.ifIsMember(userInfo);


        return ResponseEntity.ok(user);


    }

}
