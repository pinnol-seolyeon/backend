package jpabasic.pinnolbe.dto.review;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReviewQuizResDto {

    private String userId;
    private int level;
    private int order;
    private List<QuizTwinDto> quizTwins;

    @Data
    public static class QuizTwinDto {
        private String sourceQuizId;
        // private String originalQuestion;
        // private String correctAnswer;
        // private String userAnswer; //기존 answer user
        private String format;
        private String twinQuestion;
        private String twinCorrectAnswer;
        private String explanation;
    }
}
