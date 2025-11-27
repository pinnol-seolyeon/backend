package jpabasic.pinnolbe.domain.analyze.quiz;

import jpabasic.pinnolbe.dto.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection="quizNotes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "user_chapter_quizType_idx", def = "{'userId':1, 'chapterId':1, 'quizType':1}")
public class QuizNotes {
    @Id
    private String id;
    private String userId;
    private String chapterId;
    private QuizType quizType; //학습하기, 1차 복습, 2차 복습

    private List<QuizRecord> records=new ArrayList<>();

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizRecord {
        private Long quizId;
        private String quiz;
        private String userAnswer;
        private Boolean isCorrect;
        private String description;
    }


    public QuizNotes(String userId, String chapterId,List<QuizRecord> records,QuizType quizType) {
        this.userId = userId;
        this.chapterId = chapterId;
        this.records = records;
        this.quizType = quizType;
    }
}
