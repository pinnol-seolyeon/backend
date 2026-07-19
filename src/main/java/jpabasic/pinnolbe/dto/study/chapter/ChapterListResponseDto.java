package jpabasic.pinnolbe.dto.study.chapter;

import jpabasic.pinnolbe.domain.study.Chapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChapterListResponseDto {

    private Boolean isAvailable=true;
    private String sessionLogId;
    private String currentChapterId;
    private Integer currentLevel; //이어서 학습할 단계
    private List<ChapterResponseDto> chapterList; //chapterId,chapterTitle

    public ChapterListResponseDto(String sessionLogId,String currentChapterId,Integer currentLevel,List<ChapterResponseDto> chapterList) {
        this.chapterList = chapterList;
        this.currentChapterId=currentChapterId;
        this.currentLevel=currentLevel;
        this.sessionLogId=sessionLogId;
        this.isAvailable=true;
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class ChapterResponseDto{
        private String chapterId;
        private String chapterTitle;

        public static ChapterResponseDto fromEntity(Chapter chapter){
            return ChapterResponseDto.builder()
                    .chapterId(chapter.getId().toString())
                    .chapterTitle(chapter.getChapterTitle())
                    .build();
        }
    }
}
