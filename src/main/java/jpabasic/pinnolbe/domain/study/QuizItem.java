package jpabasic.pinnolbe.domain.study;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizItem {
    @Id
    private String quizId;
    private String quiz;            // 문제
    private List<String> options;   // ["O", "X"]
    private String answer;          // "O"
    private String question;
}
