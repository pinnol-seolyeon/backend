package jpabasic.pinnolbe.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TextReviewResponse {
    private String chapterId;
    private String textbook;
}