package jpabasic.pinnolbe.dto.analyze;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Data
@Getter
public class QuizAnalyzeDto {
    private String quizId;
    private String question;
    private List<String> options;
    private String correctAnswer;
    private String userAnswer;
    private Boolean isCorrect;
    private long responseTime; // 단위: ms
    private String userId;      // 유저 ID도 받아야 저장 가능
    private LocalDate quizDate;
}