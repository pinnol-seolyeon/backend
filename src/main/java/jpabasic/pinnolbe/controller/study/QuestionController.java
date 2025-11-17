package jpabasic.pinnolbe.controller.study;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.question.*;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.question.QuestionService;
import jpabasic.pinnolbe.service.question.SseService;
import jpabasic.pinnolbe.service.study.StudyLogService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/question")
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;
    private final StudyLogService studyLogService;
    private final SseService sseService;
    private final QuestionTempCache tempCache;

    public QuestionController(
        QuestionService questionService, UserService userService,
        StudyLogService studyLogService,SseService sseService,
        QuestionTempCache tempCache) {
        this.questionService = questionService;
        this.userService = userService;
        this.studyLogService = studyLogService;
        this.sseService = sseService;
        this.tempCache = tempCache;
    }


    @GetMapping(value="/stream",produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary="질문하기 실시간 응답")
    public SseEmitter streamChat(
            @RequestParam String question
    ){
        User user=userService.getUserInfo();

        //request body 생성
        QuestionRequest request=questionService.askQuestion(question,user);

        //실시간 SSE 즉시 반환
        StreamingResultDto result=sseService.askQuestionStream(request);
        return result.getEmitter();
    }

          
    @PostMapping("/save-all")
    @Operation(summary="여태까지 진행한 질문+답변 DB에 저장 및 표현력&참여도 측정",
                description= """
                        학습하기 3단계 완료 시 해당 api 호출 (session에 저장해두었던 질문/답변/점수 DB에 저장)
                        """)
    public ApiResponse<String> saveAllQA(@RequestParam String chapterId){
        User user=userService.getUserInfo();
        try {
            //질문한 내용들 DB에 저장
            List<String> questions=questionService.commitUserSession(user.getId(), chapterId);
            System.out.println("✔️ 질문한 내용들 DB에 저장 완료");
            //weeklyAnalysis에 질문개수 업데이트 (참여도)
            questionService.updateWeeklyQuestionCount(user);
            System.out.println("✔️weeklyAnalysis에 질문 개수 업데이트");
            //오늘 질문&답변 리스트 보여주기
            List<String> todayQAs = studyLogService.getTodayCollections(user.getId());
            System.out.println("✔️오늘 질문&답변 리스트 보여주기");
            //WeeklyAnalysis에 표현력 업데이트
            if (!todayQAs.isEmpty()) {
                questionService.updateExpressionScore(user, questions);
            }

        }catch(Exception e) {
            return ApiResponse.fail("질문 저장 종 오류가 발생하였습니다.",500);
        }
        return ApiResponse.success("질문 저장 완료",null);
    }


}
