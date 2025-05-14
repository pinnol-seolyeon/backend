package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.StudyLog;
import jpabasic.pinnolbe.dto.AttendanceDto;
import jpabasic.pinnolbe.dto.TodayStudyTimeDto;
import jpabasic.pinnolbe.repository.StudyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyLogService {

    private final StudyLogRepository studyLogRepository;

    public TodayStudyTimeDto getTodayStudyTime(String userId) {
        LocalDate today = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();
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

        // 1. 사용자 로그 전부 불러오기
        List<StudyLog> logs = studyLogRepository.findByUserId(userId);

        // 2. 해당 연도-월에 해당하는 날짜만 필터링하고 문자열로 변환
        Set<String> uniqueDates = logs.stream()
                .map(StudyLog::getDate)
                .filter(date -> YearMonth.from(date).equals(month))  // 연월 필터
                .map(LocalDate::toString)                            // "2025-04-12"
                .collect(Collectors.toSet());

        return new AttendanceDto(new ArrayList<>(uniqueDates));
    }

    public String getTodayStudyType(String userId) {
        LocalDate today = LocalDate.now();
        List<StudyLog> logs = studyLogRepository.findByUserIdAndDate(userId, today);

        if (logs.isEmpty()) return "랜덤형";

        Map<String, Integer> timeSlots = new HashMap<>();
        timeSlots.put("아침형", 0);
        timeSlots.put("낮형", 0);
        timeSlots.put("저녁형", 0);
        timeSlots.put("야행성", 0);

        for (StudyLog log : logs) {
            int hour = log.getStartTime().getHour();

            if (hour >= 5 && hour <= 9) timeSlots.computeIfPresent("아침형", (k, v) -> v + 1);
            else if (hour >= 10 && hour <= 16) timeSlots.computeIfPresent("낮형", (k, v) -> v + 1);
            else if (hour >= 17 && hour <= 21) timeSlots.computeIfPresent("저녁형", (k, v) -> v + 1);
            else timeSlots.computeIfPresent("야행성", (k, v) -> v + 1);
        }

        // 가장 많은 구간 찾기
        String maxType = Collections.max(timeSlots.entrySet(), Map.Entry.comparingByValue()).getKey();
        if (timeSlots.get(maxType) == 0) return "랜덤형";
        return maxType;
    }

}
