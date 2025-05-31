package jpabasic.pinnolbe.service.analyze;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.dto.analyze.RadarScoreComparisonDto;
import jpabasic.pinnolbe.dto.analyze.RadarScoreDto;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RadarScoreService {

    private final WeeklyAnalysisRepository weeklyAnalysisRepository;
    private final UserService userService;

    public RadarScoreDto getThisWeekRadarScore() {
        User user = userService.getUserInfo();
        String userId = user.getId();
        LocalDate thisWeekStart = LocalDate.now(ZoneId.of("Asia/Seoul")).with(DayOfWeek.MONDAY);

        List<WeeklyAnalysis> thisWeekDocs = weeklyAnalysisRepository
                .findAllByUserIdAndWeekStartDate(userId, thisWeekStart);

        return toRadarScore(thisWeekDocs);
    }

    public RadarScoreComparisonDto getThisAndLastWeekRadarScore() {
        User user = userService.getUserInfo();
        String userId = user.getId();
        LocalDate thisWeekStart = LocalDate.now(ZoneId.of("Asia/Seoul")).with(DayOfWeek.MONDAY);
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);

        List<WeeklyAnalysis> thisWeekDocs = weeklyAnalysisRepository
                .findAllByUserIdAndWeekStartDate(userId, thisWeekStart);
        List<WeeklyAnalysis> lastWeekDocs = weeklyAnalysisRepository
                .findAllByUserIdAndWeekStartDate(userId, lastWeekStart);

        RadarScoreComparisonDto dto = new RadarScoreComparisonDto();
        dto.setThisWeek(toRadarScore(thisWeekDocs));
        dto.setLastWeek(toRadarScore(lastWeekDocs));
        return dto;
    }

    private RadarScoreDto toRadarScore(List<WeeklyAnalysis> dataList) {
        RadarScoreDto dto = new RadarScoreDto();

        // 참여도
        int totalQuestions = dataList.stream()
                .mapToInt(d -> Optional.ofNullable(d.getEngagementData()).map(WeeklyAnalysis.EngagementData::getQuestionCount).orElse(0))
                .sum();
        dto.setEngagement(Math.min(5.0, totalQuestions * 0.5) / 5.0);

        // 이해도
        int correct = dataList.stream()
                .mapToInt(d -> Optional.ofNullable(d.getUnderstandingData()).map(WeeklyAnalysis.UnderstandingData::getCorrect).orElse(0))
                .sum();
        int total = dataList.stream()
                .mapToInt(d -> Optional.ofNullable(d.getUnderstandingData()).map(WeeklyAnalysis.UnderstandingData::getTotal).orElse(0))
                .sum();
        dto.setUnderstanding(total == 0 ? 0.0 : (double) correct / total);

        // 집중도
        double avgResponseTime = dataList.stream()
                .mapToDouble(d -> Optional.ofNullable(d.getFocusData()).map(WeeklyAnalysis.FocusData::getAverageResponseTime).orElse(0.0))
                .average()
                .orElse(0.0);
        double focusScore = Math.max(0, 5.0 - Math.max(0, avgResponseTime - 3.0));
        dto.setFocus(focusScore / 5.0);


        // 표현력
        double avgWords = dataList.stream()
                .mapToDouble(d -> Optional.ofNullable(d.getExpressionData()).map(WeeklyAnalysis.ExpressionData::getAvgWordCount).orElse(0.0))
                .average()
                .orElse(0.0);
        double expressionScore = Math.min(5.0, avgWords / 2.0);
        dto.setExpression(expressionScore / 5.0);

        return dto;
    }
}
