package jpabasic.pinnolbe.controller;

import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.User.UserInfoDto;
import jpabasic.pinnolbe.dto.login.ChildInfoDto;
import jpabasic.pinnolbe.dto.login.oauth2.CustomOAuth2User;
import jpabasic.pinnolbe.repository.UserRepository;
import jpabasic.pinnolbe.service.login.CustomOAuth2UserService;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PatchMapping("/child")
    public ResponseEntity<?> inputChildInfo(@RequestBody ChildInfoDto dto) {
        User user = userService.getUserInfo();     // 로그인한 부모
        userService.inputUserInfo(user, dto);      // 자녀 정보 업데이트
        return ResponseEntity.ok(Map.of("childId", user.getId())); // 자녀 ID 반환
    }

    //유저 정보 가져오기(헤더용)
    @GetMapping
    public ResponseEntity<UserInfoDto> getUserInfoDto() {

        System.out.println("⭐정상적으로 호출됨⭐");
        User user=userService.getUserInfo();
        //SecurityContextHolder에서 유저 정보 가져오기
        UserInfoDto userInfo=userService.getUserInfoDto(user);
        return ResponseEntity.ok(userInfo);
    }

//    @PostMapping("/logout")
//    @Operation(summary="내 서버에서 로그아웃")
//    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken) {
//
//    }
}
