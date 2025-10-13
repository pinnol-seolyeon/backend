package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.analyze.StudySessionSummaryDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.study.StudySessionService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/session")
public class StudySessionController {

    @Autowired
    private StudySessionService studySessionService;
    @Autowired
    private UserService userService;


    @PostMapping("/start-level")
    @Operation(summary="특정 레벨 공부 시작")
    public ApiResponse<String> startLevel(
            @RequestParam int level,
            @RequestParam String chapterId){

        User user=userService.getUserInfo();
        String studySessionLogId=studySessionService.startLevel(user,level,chapterId);

        return ApiResponse.success("redis에 현 공부 상태 저장을 완료했어요.",studySessionLogId);
    }

    @PostMapping("/update")
    @Operation(
            summary = "학습 상태 저장 (ACTIVE / INACTIVE / COMPLETED)",
            description = """
    사용자의 학습 세션 상태를 Redis에 갱신합니다.

    ✅ 상태별 전송 규칙:
    - **ACTIVE ↔ INACTIVE** 전환 시 → `startTime`은 무시하고 `lastActive`만 전송
    - **COMPLETED** (학습 완료 시) → `startTime` / `lastActive` 모두 무시 가능

    해당 API는 사용자의 현재 학습 상태를 Redis에 저장 및 갱신합니다.
    """
    )
    public ApiResponse<Void> sessionUpdate(
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "학습 상태 요약 DTO"
            ) StudySessionSummaryDto summary

    ){
        User user=userService.getUserInfo();
        studySessionService.sessionUpdate(user,summary);

        return ApiResponse.success("redis에 현 공부 상태 저장 갱신을 완료했어요.",null);
    }

//    @PostMapping("/complete")
//    @Operation(summary="chapter 학습 완료")
//    public ApiResponse<Void> complete(
//            @RequestBody StudySession summary
//    ){
//        User user=userService.getUserInfo();
//        StudySessionService.chapterComplete(user,summary);
//    }
//

}
