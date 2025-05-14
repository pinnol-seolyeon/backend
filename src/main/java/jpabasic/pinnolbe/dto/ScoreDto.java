package jpabasic.pinnolbe.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScoreDto {
    private String userId;
    private int score;
    private String timestamp;

}