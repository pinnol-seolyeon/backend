package jpabasic.pinnolbe.controller;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.dto.question.QAs;
import jpabasic.pinnolbe.dto.question.QuestionRequest;
import jpabasic.pinnolbe.service.QuestionService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/question")
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;

    public QuestionController(QuestionService questionService, UserService userService) {
        this.questionService = questionService;
        this.userService = userService;
    }

//    //AI에게 물어보는 API
//    @PostMapping("")
//    public ResponseEntity<String> askQuestion(@RequestBody QuestionRequest questionRequest) {
//        User user=userService.getUserInfo();
//        questionService.askQuestion(questionRequest,user);
//        return new ResponseEntity<>(question, HttpStatus.OK);
//    }

    //여태까지 진행한 질문+답변 한꺼번에 DB에 저장
    @PostMapping("/saveAll")
    public ResponseEntity<QueCollection> saveAllQA(@RequestBody QAs queAns){
        User user=userService.getUserInfo();
        return(questionService.saveAllQAs(user,queAns));
    }
}
