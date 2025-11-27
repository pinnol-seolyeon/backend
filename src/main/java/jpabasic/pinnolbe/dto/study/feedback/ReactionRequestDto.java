package jpabasic.pinnolbe.dto.study.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRequestDto {
    private String chapterId;
    private String quiz; //교재에서 ai 선생님이 묻는 질문
    private String userAnswer;
}
