package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.StudyLog;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.AttendanceDto;
import jpabasic.pinnolbe.dto.ScoreRequestDto;
import jpabasic.pinnolbe.dto.TodayStudyTimeDto;
import jpabasic.pinnolbe.dto.TodayStudyTypeResponse;
import jpabasic.pinnolbe.dto.study.FinishChaptersDto;
import jpabasic.pinnolbe.dto.study.StudyStatsDto;
import jpabasic.pinnolbe.repository.StudyLogRepository;
import jpabasic.pinnolbe.service.StudyLogService;
import jpabasic.pinnolbe.service.StudyService;
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
    private final StudyService studyService;


    @GetMapping("/stats")
    public ResponseEntity<StudyStatsDto> getStudyStats() {
        User user = userService.getUserInfo();

        StudyStatsDto stats = studyService.getStudyStats(user.getId());
        return ResponseEntity.ok(stats);
    }


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

    @GetMapping("/completed")
    @Operation(summary="학습완료한 단원 개수")
    public ResponseEntity<FinishChaptersDto> getCompletedWeek(){
        User user=userService.getUserInfo();
        String studyId=user.getStudyId();

        FinishChaptersDto dto=studyLogService.getCompletedWeek(studyId);
        return ResponseEntity.ok(dto);
    }



//    @GetMapping("/today/{userId}")
//    public TodayStudyTypeResponse getTodayStudyInfo(@PathVariable String userId) {
//        LocalDate today = LocalDate.now();
//        List<StudyLog> logs = studyLogService.getTodayStudyType(userId);
//
//        Duration total = logs.stream()
//                .map(log -> Duration.between(log.getStartTime(), log.getEndTime()))
//                .reduce(Duration.ZERO, Duration::plus);
//
//        int hours = (int) total.toHours();
//        int minutes = total.toMinutesPart();
//
//        String type = studyLogService.getTodayStudyType(userId);
//        return new TodayStudyTypeResponse(hours, minutes, type);
//    }


}

