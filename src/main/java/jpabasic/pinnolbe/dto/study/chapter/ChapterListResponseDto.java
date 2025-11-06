package jpabasic.pinnolbe.dto.study.chapter;

import jpabasic.pinnolbe.domain.study.Book;
import jpabasic.pinnolbe.domain.study.Chapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Slice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChapterListResponseDto {

    private String sessionLogId;
    private String currentChapterId;
    private Integer currentLevel; //이어서 학습할 단계
    private Slice<ChapterResponseDto> chapterList; //chapterId,chapterTitle


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
