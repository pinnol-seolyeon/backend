package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.user.PhoneRequestDto;
import jpabasic.pinnolbe.dto.user.UserInfoDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.jwt.JwtUtil;
import jpabasic.pinnolbe.repository.UserRepository;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/parents/phone-number")
    @Operation(summary="부모님 성함,전화번호 받기")
    public ApiResponse<String> registerParent(@RequestBody PhoneRequestDto dto) {
        User user = userService.getUserInfo();     // 로그인한 부모
        userService.inputUserInfo(user, dto);      // 전화번호 업데이트
        return ApiResponse.success("보호자 정보 등록 완료",null);
    }

    //유저 정보 가져오기(헤더용)
    @GetMapping
    public ResponseEntity<UserInfoDto> getUserInfoDto() {
        User user=userService.getUserInfo();
        String userId=user.getId();
        UserInfoDto userInfo=userService.getUserInfoDto(user,userId);
        return ResponseEntity.ok(userInfo);
    }


}
