package jpabasic.pinnolbe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AttendanceDto {
    private List<LocalDate> attendedDates;
}

