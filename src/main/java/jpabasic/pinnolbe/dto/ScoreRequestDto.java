package jpabasic.pinnolbe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ScoreRequestDto {
    private int unit;
    private int score;
    private int coin;
}
