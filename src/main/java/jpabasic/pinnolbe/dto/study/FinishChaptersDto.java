package jpabasic.pinnolbe.dto.study;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class FinishChaptersDto {

    private int week_count;
    private int total_count;



}
