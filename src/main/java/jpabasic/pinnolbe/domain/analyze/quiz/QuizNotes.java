package jpabasic.pinnolbe.domain.analyze.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection="quizNotes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuizNotes {
    @Id
    private String id;
    private String userId;
    private String chapterId;

    private List<QuizRecord> records=new ArrayList<>();

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizRecord {
        private String quizId;
        private String question;
        private String userAnswer;
        private Boolean isCorrect;
        private String description;
    }


    public QuizNotes(String userId, String chapterId,List<QuizRecord> records) {
        this.userId = userId;
        this.chapterId = chapterId;
        this.records = records;
    }
}
