package jpabasic.pinnolbe.dto.study.feedback;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiReactionRequestDto {

    private String userId;      // 서버가 주입하는 정보
    private String chapterId;
    private String quiz;
    private String userAnswer;

    public AiReactionRequestDto( String chapterId, String quiz, String userAnswer,String userId) {
        this.chapterId = chapterId;
        this.quiz = quiz;
        this.userAnswer = userAnswer;
        this.userId = userId;
    }
}

