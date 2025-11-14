package jpabasic.pinnolbe.domain.analyze.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="quizRecords")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizRecord {
    @Id
    private String id;
    private String quizNotesId;
    private String quizId;
    private String userAnswer;
    private Boolean isCorrect;

    public QuizRecord(String quizNotesId,String quizId, String userAnswer,Boolean isCorrect) {
        this.quizNotesId = quizNotesId;
        this.quizId = quizId;
        this.userAnswer = userAnswer;
        this.isCorrect = isCorrect;
    }


}
