package jpabasic.pinnolbe.dto.currentSituation;

import jpabasic.pinnolbe.domain.CurrentSituationStatus;
import jpabasic.pinnolbe.domain.Status;
import jpabasic.pinnolbe.domain.badge.BadgeType;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.dto.study.chapter.ChapterListResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@Builder
public class CurrentSituationResDto {

    private Double totalProgress=0.0;

    @Getter
    @Setter
    public static class CurrentChapterRes extends ChapterListResponseDto.ChapterResponseDto {
        private CurrentSituationStatus status; //해당 단원 학습 상태
        private List<BadgeType> badgeType; //획득한 배지
        private Double progress; //학습 진행률
        private int order; //chapter 순서

        public CurrentChapterRes(String chapterId, String chapterTitle,int order,List<BadgeType> badgeType) {
            super(chapterId, chapterTitle);
            this.order = order;
            this.badgeType = badgeType;
        }

        public CurrentChapterRes(Chapter chapter) {
            super(chapter.getId().toString(),chapter.getChapterTitle());
            this.order=chapter.getOrder();
        }

        public static CurrentChapterRes fromEntity(Chapter chapter,int order,List<BadgeType> badgeType) {
            return new CurrentChapterRes(
                    chapter.getId().toString(),
                    chapter.getChapterTitle(),
                    order,
                    badgeType
            );
        }

        public static CurrentChapterRes toDto(Chapter chapter) {
            return new CurrentChapterRes(
                    chapter
            );
        }
    }

}
