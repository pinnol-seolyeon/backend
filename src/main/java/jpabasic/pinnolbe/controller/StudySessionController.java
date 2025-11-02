package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.analyze.StudySessionLogResponseDto;
import jpabasic.pinnolbe.dto.analyze.StudySessionSummaryDto;
import jpabasic.pinnolbe.dto.study.ChapterDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.analyze.WeeklyAnalysisService;
import jpabasic.pinnolbe.service.study.StudyService;
import jpabasic.pinnolbe.service.study.StudySessionService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class StudySessionController {

    @Autowired
    private StudySessionService studySessionService;
    @Autowired
    private UserService userService;
    @Autowired
    private StudyService studyService;
    @Autowired
    private WeeklyAnalysisService weeklyAnalysisService;


    @PostMapping("/start-level")
    @Operation(summary="특정 레벨 공부 시작",
                description= """
                        현재는 하드코딩된 내용을 임의로 가져와서 localStorage에 저장해놓음.
                        ai 적용 시, 로직 수정될 예정
                        """)
    public ApiResponse<Map<String,Object>> startLevel(
            @RequestParam int level,
            @RequestParam String chapterId){

        User user=userService.getUserInfo();
        //학습 상태 저장할 Redis(세부 학습내용), studysessionLog(현 학습 상황) 생성 및 확인
        String studySessionLogId=studySessionService.startLevel(user,level,chapterId);
        //학습할 내용 가져오기
        Map<String,Object> levelContents=studyService.getChapterContents(chapterId,level);
        levelContents.put("studySessionLogId",studySessionLogId);

        return ApiResponse.success("redis에 현 공부 상태 저장을 완료했어요.",levelContents);
    }

    @PostMapping("/update")
    @Operation(
            summary = "학습 상태 저장 (ACTIVE / INACTIVE / COMPLETED / EXIT)",
            description = """
    사용자의 학습 세션 상태를 Redis에 갱신합니다.

    ✅ 상태별 전송 규칙:
    - **ACTIVE ↔ INACTIVE** 전환 시 → `startTime`은 무시하고 `lastActive`만 전송
    - **COMPLETED** (학습 완료 시) → `startTime` / `lastActive` 모두 무시 가능
    
    ✅ 6단계까지 해당 chapter 학습 완료 시:
    - isCompleted=true

    해당 API는 사용자의 현재 학습 상태를 Redis에 저장 및 갱신합니다.
    """
    )
    public ApiResponse<StudySessionLogResponseDto> sessionUpdate(
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "학습 상태 요약 DTO"
            ) StudySessionSummaryDto summary

    ){
        User user=userService.getUserInfo();
        if(!summary.isCompleted()){ //isCompleted=false인 경우 //아직 해당 단원 학습 완료X
            StudySessionLogResponseDto result=studySessionService.sessionUpdate(user,summary);
            return ApiResponse.success("redis에 현 공부 상태 저장 갱신을 완료했어요.",result);
        }else{
            //COMPLETED 처리 로직
            StudySessionLogResponseDto result=studySessionService.sessionUpdate(user,summary);
            String weeklyId=result.getWeeklyAnalysisId();

            //해당 단원 학습 모두 완료한 경우
            studyService.finishChapter(user,summary);
            //해당 chapter studySessionLog 모두 삭제

            //완료한 단원 weeklyAnalysis에 저장
            weeklyAnalysisService.saveCompletedChapters(user,summary.getChapterId(),weeklyId);
            return ApiResponse.success("해당 chapter 학습을 완료했어요.",null);
        }

    }



}
