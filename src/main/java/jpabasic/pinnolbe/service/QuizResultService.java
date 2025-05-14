package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.dto.QuizResultDto;
import jpabasic.pinnolbe.repository.QuizResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizResultService {

    private final QuizResultRepository quizRepo;

    public List<QuizResultDto> getQuizResults(String userId) {
        return quizRepo.findByUserIdOrderByDateAsc(userId).stream()
                .map(result -> new QuizResultDto(
                        result.getDate().toString(),
                        result.getWrong(),
                        result.getQuestionMark()
                )).collect(Collectors.toList());
    }
}
