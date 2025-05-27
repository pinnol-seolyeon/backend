package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.dto.question.QAs;
import jpabasic.pinnolbe.dto.question.QuestionRequest;
import jpabasic.pinnolbe.dto.question.QuestionResponse;
import jpabasic.pinnolbe.service.QuestionService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/question")
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;

    public QuestionController(QuestionService questionService, UserService userService) {
        this.questionService = questionService;
        this.userService = userService;
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
        }catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.ok("여태까지의 질문&답변이 DB에 무사히 저장되었습니다.");
    }

    // 질문한 개수


}
