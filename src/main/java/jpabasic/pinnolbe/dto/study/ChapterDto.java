package jpabasic.pinnolbe.dto.study;

import jpabasic.pinnolbe.domain.study.Chapter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ChapterDto {
    private String chapterId;
    private String chapterTitle;
    private String content;
    private String objective;
    private String imgUrl;
    private String summaryImgUrl;
    private String objectiveQuestion;
    private String objectiveAnswer;
    private String summary;
    private String topic;

    //static : 객체를 만들지 않아도 사용 가능
    public static ChapterDto convertDto(String chapterId, Chapter chapter){
        ChapterDto dto = new ChapterDto();
        dto.setChapterId(chapterId);
        dto.setChapterTitle(chapter.getChapterTitle());
        dto.setContent(chapter.getContent());
        dto.setObjective(chapter.getObjective());
        dto.setImgUrl(chapter.getImgUrl());
        dto.setSummaryImgUrl(chapter.getSummaryImgUrl());
        dto.setObjectiveQuestion(chapter.getObjectiveQuestion());
        dto.setObjectiveAnswer(chapter.getObjectiveAnswer());
        dto.setSummary(chapter.getSummary());
        dto.setTopic(chapter.getTopic());
        return dto;
    }
}
