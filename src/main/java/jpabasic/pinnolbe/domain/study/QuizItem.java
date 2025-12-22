package jpabasic.pinnolbe.domain.study;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizItem {
    private Long quizId;
    private String question;            // 문제
    private List<String> options;   // ["O", "X"]
    private String answer;          // "O"
    private String description;
}
