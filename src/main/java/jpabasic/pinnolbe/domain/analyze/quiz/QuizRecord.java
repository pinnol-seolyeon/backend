package jpabasic.pinnolbe.domain.analyze.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="quiz_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizRecord {
    @Id
    private String id;
    private String quizNotesId;
    private String quizId;
    private String correctAnswer;
    private String userAnswer;

    public QuizRecord(String quizId, String correctAnswer, String userAnswer) {
        this.quizNotesId = quizNotesId;
        this.quizId = quizId;
        this.correctAnswer = correctAnswer;
        this.userAnswer = userAnswer;
    }

    public QuizRecord toEntity(){
        QuizRecord q = new QuizRecord();
        q.setQuizId(quizId);
        q.setCorrectAnswer(correctAnswer);
        q.setUserAnswer(userAnswer);
        return q;
    }
}
