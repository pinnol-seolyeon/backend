package jpabasic.pinnolbe.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@Getter
@RequiredArgsConstructor
public class QuizReviewResponse {
    // private String sourceQuizId;
    private String format;
    private String twinQuestion;
    private String twinCorrectAnswer;
    private String explanation;

    public QuizReviewResponse(String format, String twinQuestion, String twinCorrectAnswer, String explanation) {
        this.format = format;
        this.twinQuestion = twinQuestion;
        this.twinCorrectAnswer = twinCorrectAnswer;
        this.explanation = explanation;
    }
}
