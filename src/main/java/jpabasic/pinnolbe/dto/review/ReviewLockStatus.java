package jpabasic.pinnolbe.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class ReviewLockStatus {
    private ReviewStatus firstReviewUnlocked;
    private ReviewStatus secondReviewUnlocked;

}
