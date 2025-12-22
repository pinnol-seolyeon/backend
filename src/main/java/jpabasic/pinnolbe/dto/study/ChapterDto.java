package jpabasic.pinnolbe.dto.study;

import java.util.List;

import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.domain.study.QuizItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ChapterDto {
    private String studySessionLogId;
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
    private List<QuizItem> quizItems;

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
        dto.setQuizItems(chapter.getQuizzes());
        return dto;
    }
}
