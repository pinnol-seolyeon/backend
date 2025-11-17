package jpabasic.pinnolbe.service.analyze;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
public class WeeklyAnalysisService {

    private final WeeklyAnalysisRepository weeklyAnalysisRepository;

    public WeeklyAnalysisService(WeeklyAnalysisRepository weeklyAnalysisRepository) {
        this.weeklyAnalysisRepository = weeklyAnalysisRepository;
    }

    public WeeklyAnalysis findThisWeekAnalysis(String userId) {
        LocalDate weekStart = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(DayOfWeek.MONDAY);

        return weeklyAnalysisRepository.findByUserIdAndWeekStartDate(userId, weekStart)
                .orElseGet(() -> new WeeklyAnalysis(userId, weekStart));
    }

    public void saveCompletedChapters(User user, String chapterId,String weeklyAnalysisId){
        LocalDate weekStart = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(DayOfWeek.MONDAY);
        LocalDate today=LocalDate.now(ZoneId.of("Asia/Seoul"));

        WeeklyAnalysis analysis =
                weeklyAnalysisRepository.findByUserIdAndWeekStartDate(user.getId(), weekStart)
                        .orElseGet(() -> new WeeklyAnalysis(user.getId(), weekStart));

        //기존 완료 리스트 가져오기
        List<WeeklyAnalysis.CompletedChapter> completed=analysis.getCompletedChapters();

        //이미 저장된 chapterId가 존재하는지 검사
        boolean exists=completed.stream()
                        .anyMatch(c->c.getChapterId().equals(chapterId));
        if(!exists){
            analysis.getCompletedChapters().add(
                    new WeeklyAnalysis.CompletedChapter(chapterId,today)
            );
        }

        weeklyAnalysisRepository.save(analysis);
    }
}
