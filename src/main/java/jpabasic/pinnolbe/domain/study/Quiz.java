package jpabasic.pinnolbe.domain.study;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "quiz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {
    @Id
    private String id;

    private String chapterId;       // 단원 ObjectId (String 형태 저장)
    private String quiz;            // 문제
    private List<String> options;   // ["O", "X"]
    private String answer;          // "O"
}
