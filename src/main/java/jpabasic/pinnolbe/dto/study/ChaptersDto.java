package jpabasic.pinnolbe.dto.study;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class ChaptersDto {

    public String id;
    public String title;

    //현재 진도
    public Boolean isCurrent;
    //학습완료한 단원
    public Boolean isCompleted;

    public ChaptersDto(String id,String title) {
        this.id = id;
        this.title = title;
    }




}
