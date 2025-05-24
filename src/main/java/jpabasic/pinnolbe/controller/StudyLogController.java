package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.StudyLog;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.AttendanceDto;
import jpabasic.pinnolbe.dto.TodayStudyTimeDto;
import jpabasic.pinnolbe.dto.TodayStudyTypeResponse;
import jpabasic.pinnolbe.dto.study.FinishChaptersDto;
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

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyLogController {

    private final StudyLogService studyLogService;
    private final UserService userService;

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

