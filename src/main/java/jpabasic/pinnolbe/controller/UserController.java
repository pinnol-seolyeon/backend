package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.user.MyPageUserResponse;
import jpabasic.pinnolbe.dto.user.PhoneRequestDto;
import jpabasic.pinnolbe.dto.user.UserInfoDto;
import jpabasic.pinnolbe.dto.user.UserUpdateRequest;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    @Operation(summary="유저 정보 가져오기(헤더용)")
    public ResponseEntity<UserInfoDto> getUserInfoDto() {
        User user=userService.getUserInfo();
        String userId=user.getId();
        UserInfoDto userInfo=userService.getUserInfoDto(user,userId);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping
    @Operation(summary="유저 정보 조회")
    public ApiResponse<MyPageUserResponse> getMyPageUserInfo(){
        User user=userService.getUserInfo();
        return ApiResponse.success("유저 정보 조회 완료",userService.getMyPageUserInfo(user));
    }

    @PatchMapping
    @Operation(summary = "유저 정보 수정",description = "마이페이지에서 유저의 프로필 정보를 수정합니다.")
    public ApiResponse<MyPageUserResponse> updateMyPageUserInfo(
            @RequestBody UserUpdateRequest request
            ){
        User user=userService.getUserInfo();
        MyPageUserResponse updatedUser=userService.updateUserInfo(user,request);
        return ApiResponse.success("유저 정보 수정 완료",updatedUser);
    }





}
