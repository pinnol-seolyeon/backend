package jpabasic.pinnolbe.controller;

import jpabasic.pinnolbe.domain.StudyLog;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.AttendanceDto;
import jpabasic.pinnolbe.dto.ScoreRequestDto;
import jpabasic.pinnolbe.dto.TodayStudyTimeDto;
import jpabasic.pinnolbe.dto.TodayStudyTypeResponse;
import jpabasic.pinnolbe.repository.StudyLogRepository;
import jpabasic.pinnolbe.service.StudyLogService;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyLogController {

    private final StudyLogService studyLogService;
    private final UserService userService;

    @GetMapping("/today")
    public ResponseEntity<?> getTodayStudyTime() {
        User user = userService.getUserInfo();
        TodayStudyTimeDto result = studyLogService.getTodayStudyTime(user.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/calendar")
    public ResponseEntity<AttendanceDto> getAttendance(
            @RequestParam int year,
            @RequestParam int month
    ) {
        User user = userService.getUserInfo();
        YearMonth yearMonth = YearMonth.of(year, month);
        AttendanceDto dto = studyLogService.getAttendanceForMonth(user.getId(), yearMonth);
        return ResponseEntity.ok(dto);
    }

}

