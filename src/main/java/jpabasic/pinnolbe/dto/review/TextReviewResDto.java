package jpabasic.pinnolbe.dto.review;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class TextReviewResDto {

    private String userId;
    private int level;
    private int order;
    private String textbook;
    private int learningHistoryCount;
}
