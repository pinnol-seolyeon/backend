package jpabasic.pinnolbe.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection="chapter_progress")
@CompoundIndex(
        name="uidx_user_chapter",
        def="{'userId':1,'chapterId':1}",
        unique=true
)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChapterProgress {
    @Id
    private String id;
    private String userId;
    private String chapterId;

    private LocalDate completedAt; //학습하기 완료 날짜
    private LocalDate firstReviewCompletedAt=null; //1차 복습 완료 날짜
    private LocalDate secondReviewCompletedAt=null; //2차 복습 완료 날짜

    public ChapterProgress(
            String chapterId,
            LocalDate today,
            String userId
    ) {
        this.chapterId = chapterId;
        this.userId = userId;
        this.completedAt = today;
        this.firstReviewCompletedAt = null;
        this.secondReviewCompletedAt = null;
    }
}
