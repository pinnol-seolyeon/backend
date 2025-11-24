package jpabasic.pinnolbe.dto.study.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiFeedBackRequestDto {

    private String userId;      // 서버가 주입하는 정보
    private String chapterId;
    private String quiz;
    private String userAnswer;

    public AiFeedBackRequestDto( String chapterId, String quiz, String userAnswer,String userId) {
        this.chapterId = chapterId;
        this.quiz = quiz;
        this.userAnswer = userAnswer;
        this.userId = userId;
    }
}

