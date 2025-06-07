package jpabasic.pinnolbe.service.analyze;

import jpabasic.pinnolbe.domain.study.Quiz;
import jpabasic.pinnolbe.repository.study.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;

    public List<Quiz> getQuiz(String chapterId, int limit) {
        List<Quiz> all = quizRepository.findByChapterId(chapterId);
        System.out.println("✅ 찾은 퀴즈 개수: " + (all == null ? "null" : all.size()));
        if (all == null) return new ArrayList<>();
        Collections.shuffle(all);
        return all.stream().limit(limit).collect(Collectors.toList());
    }
}
