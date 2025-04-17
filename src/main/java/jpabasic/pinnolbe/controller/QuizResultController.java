package jpabasic.pinnolbe.controller;

import jpabasic.pinnolbe.dto.QuizResultDto;
import jpabasic.pinnolbe.service.QuizResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizResultController {

    private final QuizResultService quizService;

    @GetMapping("/results")
    public ResponseEntity<List<QuizResultDto>> getQuizResults(@RequestParam String userId) {
        List<QuizResultDto> results = quizService.getQuizResults(userId);
        return ResponseEntity.ok(results);
    }
}
