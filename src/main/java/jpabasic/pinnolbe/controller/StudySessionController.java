package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.StudySession;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.StudySessionSummaryDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.StudySessionService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/session")
public class StudySessionController {

    @Autowired
    private StudySessionService studySessionService;
    private UserService userService;


    @PostMapping("/start-level")
    @Operation(summary="특정 레벨 공부 시작")
    public ApiResponse<Void> startLevel(
            @RequestParam int level,
            @RequestParam String chapterId){

        User user=userService.getUserInfo();
        studySessionService.startLevel(user,level,chapterId);

        return ApiResponse.success("redis에 현 공부 상태 저장을 완료했어요.",null);
    }

    @PostMapping("/summary")
    @Operation(summary="학습 중 summary 반영")
    public ApiResponse<Void> sessionUpdate(
            @RequestBody StudySessionSummaryDto summary
    ){
        User user=userService.getUserInfo();
        studySessionService.sessionUpdate(user,summary);

        return ApiResponse.success("redis에 현 공부 상태 저장 갱신을 완료했어요.",null);
    }

//    @PostMapping("/complete")
//    @Operation(summary="학습 완료 (세션 삭제)")
//    public ApiResponse<Void> complete(
//            @RequestBody StudySession summary
//    ){
//        User user=userService.getUserInfo();
//        StudySessionService.sessionComplete(user,summary);
//    }
//
//    @PostMapping("/force-delete")
//    @Operation(summary="INACTIVE 세션 강제 삭제")
}
