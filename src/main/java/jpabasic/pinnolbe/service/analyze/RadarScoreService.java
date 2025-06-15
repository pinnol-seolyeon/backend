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
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RadarScoreService {

    private final WeeklyAnalysisRepository weeklyAnalysisRepository;
    private final UserService userService;

    public RadarScoreDto getThisWeekRadarScore() {
        User user = userService.getUserInfo();
        String userId = user.getId();
        LocalDate thisWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);

        List<WeeklyAnalysis> thisWeekDocs = weeklyAnalysisRepository
                .findAllByUserIdAndWeekStartDate(userId, thisWeekStart);

        return toRadarScore(thisWeekDocs);
    }

    public RadarScoreComparisonDto getThisAndLastWeekRadarScore() {
        User user = userService.getUserInfo();
        String userId = user.getId();
        LocalDate thisWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
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

        // 이해도 (누적 correct/total)
        int correct = dataList.stream()
                .mapToInt(d -> Optional.ofNullable(d.getUnderstandingData())
                        .map(WeeklyAnalysis.UnderstandingData::getCorrect)
                        .orElse(0))
                .sum();
        int total = dataList.stream()
                .mapToInt(d -> Optional.ofNullable(d.getUnderstandingData())
                        .map(WeeklyAnalysis.UnderstandingData::getTotal)
                        .orElse(0))
                .sum();
        dto.setUnderstanding(total == 0 ? 0.0 : (double) correct / total);

        // 집중도 (sumResponseTime/count 기반 가중평균)
        double totalSumSec = dataList.stream()
                .map(WeeklyAnalysis::getFocusData)
                .filter(Objects::nonNull)
                .mapToDouble(WeeklyAnalysis.FocusData::getSumResponseTime)
                .sum();
        int totalCount = dataList.stream()
                .map(WeeklyAnalysis::getFocusData)
                .filter(Objects::nonNull)
                .mapToInt(WeeklyAnalysis.FocusData::getCount)
                .sum();

        double avgResponseTime = totalCount == 0
                ? Double.NaN
                : totalSumSec / totalCount;

        double focus;
        if (Double.isNaN(avgResponseTime)) {
            focus = 0.0;
        } else {
            // 채점 기준: 3초 이하는 만점, 이후 선형 감점
            double focusScore = Math.max(0, 5.0 - Math.max(0, avgResponseTime - 3.0));
            focus = focusScore / 5.0;
        }
        dto.setFocus(focus);

        // 표현력
        double avgStarScore = dataList.stream()
                .mapToInt(d -> Optional.ofNullable(d.getExpressionData())
                        .map(WeeklyAnalysis.ExpressionData::getStarScore)
                        .orElse(0))
                .average()
                .orElse(0.0);

        double expressionScore = Math.min(5.0, avgStarScore); // 혹시 모르니 제한 유지
        dto.setExpression(expressionScore / 5.0); // 정규화

        return dto;
    }
}
