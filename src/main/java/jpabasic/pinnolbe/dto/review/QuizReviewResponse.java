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
    private Long sourceQuizId;
    private String twinQuestion;
    private String correctAnswer;
    private String explanation;

    public QuizReviewResponse(Long sourceQuizId, String twinQuestion, String explanation) {
        this.sourceQuizId = sourceQuizId;
        this.twinQuestion = twinQuestion;
        this.explanation = explanation;
        this.correctAnswer = "";
    }
}
