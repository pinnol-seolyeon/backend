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

    public void saveCompletedChapters(User user, String chapterId,String weeklyAnalysisId){
        LocalDate weekStart = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(DayOfWeek.MONDAY);
        WeeklyAnalysis analysis =
                weeklyAnalysisRepository.findByUserIdAndWeekStartDate(user.getId(), weekStart)
                        .orElseGet(() -> new WeeklyAnalysis(user.getId(), weekStart));

        //기존 완료 리스트 가져오기
        List<String> completed=analysis.getCompletedChapters();
        if(!completed.contains(chapterId)) {
            completed.add(chapterId);
        }


        analysis.setCompletedChapters(completed);
        weeklyAnalysisRepository.save(analysis);
    }
}
