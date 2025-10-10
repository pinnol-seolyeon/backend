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

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserRepository userRepository,
                          UserService userService,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PatchMapping("/parents/phone-number")
    @Operation(summary="부모님 전화번호 받기")
    public ApiResponse<String> registerParent(@RequestBody PhoneRequestDto dto) {
        User user = userService.getUserInfo();     // 로그인한 부모
        userService.inputUserInfo(user, dto);      // 전화번호 업데이트
        return ApiResponse.success("보호자 정보 등록 완료",null);
    }

    //유저 정보 가져오기(헤더용)
    @GetMapping
    public ResponseEntity<UserInfoDto> getUserInfoDto() {

//        System.out.println("⭐정상적으로 호출됨⭐");
        User user=userService.getUserInfo();
        //SecurityContextHolder에서 유저 정보 가져오기
        UserInfoDto userInfo=userService.getUserInfoDto(user);
        return ResponseEntity.ok(userInfo);
    }


}
