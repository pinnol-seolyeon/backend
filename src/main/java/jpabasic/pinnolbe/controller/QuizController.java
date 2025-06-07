package jpabasic.pinnolbe.controller;

import jpabasic.pinnolbe.domain.study.Quiz;
import jpabasic.pinnolbe.service.analyze.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    public List<Quiz> getQuiz(@RequestParam String chapterId) {
        return quizService.getQuiz(chapterId, 5);  // 최대 5개 리턴
    }
}