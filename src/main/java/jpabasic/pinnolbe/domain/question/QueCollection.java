package jpabasic.pinnolbe.domain.question;

import jpabasic.pinnolbe.domain.BaseEntity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection="queCollection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueCollection extends BaseEntity {

    @Id
    private String id;
    private String userId;

    private LocalDate date; //질문한 날짜
    private String chapterId; //단원

    private List<String> questions;
    private List<String> answers;



}
