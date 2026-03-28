package jpabasic.pinnolbe.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class HoldingPeriod {
    private LocalDate holdStartDate;
    private LocalDate holdEndDate;
    private long durationDays;

}
