package jpabasic.pinnolbe.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@Data
@Getter
@RequiredArgsConstructor
public class QuizReviewResponse {
    private String sourceQuizId;
    private String twinQuestion;
    private String correctAnswer;
    private String explanation;

    public QuizReviewResponse(String sourceQuizId, String twinQuestion, String explanation) {
        this.sourceQuizId = sourceQuizId;
        this.twinQuestion = twinQuestion;
        this.explanation = explanation;
        this.correctAnswer = "";
    }
}
