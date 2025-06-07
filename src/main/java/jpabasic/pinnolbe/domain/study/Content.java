package jpabasic.pinnolbe.domain.study;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "edu_content")
@Getter
@Setter
@NoArgsConstructor
public class Content {
    @Id
    private String id;

    private String text;        // TTS로 합성할 실제 텍스트 한 줄
    private String lectureId;   // (선택) 같은 강의 묶음을 위해 사용할 수 있는 필드
    private Integer sequence;   // (선택) 순차적으로 읽을 때 사용할 순서
}

