package jpabasic.pinnolbe.service.analyze;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.dto.analyze.RadarScoreComparisonDto;
import jpabasic.pinnolbe.dto.analyze.RadarScoreDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
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

        WeeklyAnalysis analysis = weeklyAnalysisRepository.findByUserIdAndWeekStartDate(userId, thisWeekStart)
                .orElseThrow(() -> new CustomException(ErrorCode.WEEKLY_ANALYSIS_NOT_FOUND));

        return toRadarScore(analysis);
    }

    public RadarScoreComparisonDto getThisAndLastWeekRadarScore() {
        User user = userService.getUserInfo();
        String userId = user.getId();
        LocalDate thisWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);

        WeeklyAnalysis thisWeek = weeklyAnalysisRepository.findByUserIdAndWeekStartDate(userId, thisWeekStart)
                .orElseThrow(() -> new CustomException(ErrorCode.WEEKLY_ANALYSIS_NOT_FOUND));
        WeeklyAnalysis lastWeek = weeklyAnalysisRepository
                .findByUserIdAndWeekStartDate(userId, lastWeekStart)
                .orElse(null);

        RadarScoreComparisonDto dto = new RadarScoreComparisonDto();
        dto.setThisWeek(toRadarScore(thisWeek));
        dto.setLastWeek(lastWeek!=null?toRadarScore(lastWeek):null);
        return dto;
    }

    private RadarScoreDto toRadarScore(WeeklyAnalysis analysis) {
        RadarScoreDto dto = new RadarScoreDto();

        // 참여도
        int totalQuestions = analysis.getEngagementData().getQuestionCount();
        dto.setEngagement(Math.min(5.0, totalQuestions * 0.5));

        // 이해도 (누적 correct/total)
        int correct = analysis.getUnderstandingData().getCorrect();
        int total = analysis.getUnderstandingData().getTotal();

        double understandingScore = total == 0 ? 0.0 : ((double) correct / total) * 5.0;
        dto.setUnderstanding(understandingScore);

        // 집중도
        double focusingScore=5;
        double deductingScore = analysis.getFocusData().getFocusingScore();

        //completedChapters=0인 경우, focusingScore는 0점.(측정된 게 아무것도 없기때문)
        if (analysis.getCompletedChapters().isEmpty()) {
            dto.setFocus(null);
        }else{
            focusingScore-=deductingScore;
            if(focusingScore<0) {
                focusingScore = 0;
            }
            focusingScore = focusingScore / analysis.getCompletedChapters().size();
            dto.setFocus(focusingScore);
        }

        // 표현력
        double expressionScore = analysis.getExpressionData().getExpressionScore();
        dto.setExpression(expressionScore); // 정규화
        return dto;
    }
}
