package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.dto.question.QuestionRequest;
import jpabasic.pinnolbe.dto.question.QuestionResponse;
import jpabasic.pinnolbe.dto.question.QuestionSummaryDto;
import jpabasic.pinnolbe.service.question.QuestionService;
import jpabasic.pinnolbe.service.study.StudyLogService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/question")
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;
    private final StudyLogService studyLogService;

    public QuestionController(QuestionService questionService, UserService userService, StudyLogService studyLogService) {
        this.questionService = questionService;
        this.userService = userService;
        this.studyLogService = studyLogService;
    }


    @PostMapping("")
    @Operation(summary="AI에게 물어보기(모델호출)")
    public ResponseEntity<QuestionResponse> askQuestion(@RequestBody QuestionRequest questionRequest) {
        User user=userService.getUserInfo();
        //AI로부터 응답받기
        QuestionResponse response=questionService.askQuestion(questionRequest,user);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/saveAll (수정 전)")
    @Operation(summary="여태까지 진행한 질문+답변 DB에 저장",
                description= """
                        학습하기 3단계 완료 시 해당 api 호출 (session에 저장해두었던 질문/답변/점수 DB에 저장)
                        """)
    public ResponseEntity<String> saveAllQA(@RequestParam String chapterId){
        User user=userService.getUserInfo();
        try {
            //질문한 내용들 DB에 저장
            List<String> questions=questionService.saveAllQAs(user, chapterId);
            //weeklyAnalysis에 질문개수 업데이트 (참여도)
            questionService.updateWeeklyQuestionCount(user);
            //오늘 질문&답변 리스트 보여주기
            List<String> todayQAs = studyLogService.getTodayCollections(user.getId());
            //WeeklyAnalysis에 표현력 업데이트
            if (!todayQAs.isEmpty()) {
                questionService.updateExpressionScore(user, questions);
            }

        }catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.ok("여태까지의 질문&답변이 DB에 무사히 저장되었습니다.");
    }


}
