package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.StudyLog;
import jpabasic.pinnolbe.dto.AttendanceDto;
import jpabasic.pinnolbe.dto.TodayStudyTimeDto;
import jpabasic.pinnolbe.repository.StudyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyLogService {

    private final StudyLogRepository studyLogRepository;

    public TodayStudyTimeDto getTodayStudyTime(String userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        List<StudyLog> logs = studyLogRepository.findByUserIdAndStartTimeBetween(userId, startOfDay, endOfDay);

        long totalMinutes = logs.stream()
                .mapToLong(log -> Duration.between(log.getStartTime(), log.getEndTime()).toMinutes())
                .sum();

        int hours = (int) totalMinutes / 60;
        int minutes = (int) totalMinutes % 60;

        return new TodayStudyTimeDto(hours, minutes);
    }

    public AttendanceDto getAttendanceForMonth(String userId, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();

        List<StudyLog> logs = studyLogRepository.findByUserIdAndStartTimeBetween(userId, start, end);

        Set<String> uniqueDates = logs.stream()
                .map(log -> log.getStartTime().toLocalDate().toString())  // "2025-04-12"
                .collect(Collectors.toSet());

        return new AttendanceDto(new ArrayList<>(uniqueDates));
    }

}
