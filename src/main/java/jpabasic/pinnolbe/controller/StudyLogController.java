package jpabasic.pinnolbe.controller;

import jpabasic.pinnolbe.domain.StudyLog;
import jpabasic.pinnolbe.dto.AttendanceDto;
import jpabasic.pinnolbe.dto.TodayStudyTimeDto;
import jpabasic.pinnolbe.repository.StudyLogRepository;
import jpabasic.pinnolbe.service.StudyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyLogController {

    private final StudyLogService studyLogService;

    @GetMapping("/today")
    public ResponseEntity<TodayStudyTimeDto> getTodayStudyTime(@RequestParam String userId) {
        return ResponseEntity.ok(studyLogService.getTodayStudyTime(userId));
    }

    @GetMapping("/calendar")
    public ResponseEntity<AttendanceDto> getAttendance(
            @RequestParam String userId,
            @RequestParam int year,
            @RequestParam int month) {

        YearMonth targetMonth = YearMonth.of(year, month);
        AttendanceDto dto = studyLogService.getAttendanceForMonth(userId, targetMonth);
        return ResponseEntity.ok(dto);
    }


}

