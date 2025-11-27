package jpabasic.pinnolbe.dto.quiz;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Data
@Getter
public class QuizAnalyzeDto {
    private Long quizId;
    private String quiz;
    private List<String> options;
    private String correctAnswer;
    private String userAnswer;
    private Boolean isCorrect;
    private LocalDate quizDate;
    private String description;
}