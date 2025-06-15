package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.dto.question.QAs;
import jpabasic.pinnolbe.dto.question.QuestionRequest;
import jpabasic.pinnolbe.dto.question.QuestionResponse;
import jpabasic.pinnolbe.dto.question.QuestionSummaryDto;
import jpabasic.pinnolbe.service.QuestionService;
import jpabasic.pinnolbe.service.StudyLogService;
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

        //해당 단원에서 첫 질문인 경우,
        QuestionResponse response=questionService.askQuestion(questionRequest,user);
        return ResponseEntity.ok(response);
    }


    //여태까지 진행한 질문+답변 한꺼번에 DB에 저장 //해당 단원 학습 완료 시 호출
    @PostMapping("/saveAll")
    @Operation(summary="여태까지 진행한 질문+답변 한꺼번에 DB에 저장")
    public ResponseEntity<String> saveAllQA(@RequestParam String chapterId){
        User user=userService.getUserInfo();
        try {
            questionService.saveAllQAs(user, chapterId);
            questionService.updateWeeklyQuestionCount(user);

            List<String> todayQAs = studyLogService.getTodayCollections(user.getId());
            if (!todayQAs.isEmpty()) {
                QuestionSummaryDto summaryDto = studyLogService.summaryQuestion(todayQAs, user);
                int expressionScore = extractExpressionScore(summaryDto.getSummary());
                questionService.updateExpressionScore(user, expressionScore);
            }

        }catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.ok("여태까지의 질문&답변이 DB에 무사히 저장되었습니다.");
    }

    // AI 응답에서 별점 숫자만 가져오기
    public int extractExpressionScore(String summaryText) {
        Pattern pattern = Pattern.compile("별점.*\\((\\d)점\\)");
        Matcher matcher = pattern.matcher(summaryText);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

}
