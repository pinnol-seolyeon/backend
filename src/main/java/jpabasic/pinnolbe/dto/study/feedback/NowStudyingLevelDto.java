package jpabasic.pinnolbe.dto.study.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NowStudyingLevelDto {

    private int bookLevel;
    private String bookTitle;
    private String chapterId;
    private String chapterTitle;
    private int currentLevel;


}
