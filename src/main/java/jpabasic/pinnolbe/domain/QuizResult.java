package jpabasic.pinnolbe.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

    @Document(collection = "quiz_results")
    @Getter
    @Setter
    @NoArgsConstructor
    public class QuizResult {
        @Id
        private String id;
        private String userId;
        private LocalDate date;
        private int correct;
        private int wrong;
        private int score;
    }
