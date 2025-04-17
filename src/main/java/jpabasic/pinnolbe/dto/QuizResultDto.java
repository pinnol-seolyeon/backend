package jpabasic.pinnolbe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class QuizResultDto {
    private String date;
    private int wrong;
    private int score;
}
