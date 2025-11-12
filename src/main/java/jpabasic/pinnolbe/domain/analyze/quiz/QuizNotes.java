package jpabasic.pinnolbe.domain.analyze.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection="quiz_notes")
@Getter
@NoArgsConstructor
public class QuizNotes {
    @Id
    private String id;
    private String userId;
    private String chapterId;
}
