package jpabasic.pinnolbe.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewListResDto {
    private String chapterId;
    private String chapterTitle;
    private ReviewLockStatus lockStatus;

}
