package jpabasic.pinnolbe.service.analyze;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class WeeklyAnalysisService {

    private final WeeklyAnalysisRepository weeklyAnalysisRepository;

    public WeeklyAnalysisService(WeeklyAnalysisRepository weeklyAnalysisRepository) {
        this.weeklyAnalysisRepository = weeklyAnalysisRepository;
    }

    public void saveCompletedChapters(User user, String chapterId){
        WeeklyAnalysis analysis=weeklyAnalysisRepository.findByUserId(user.getId())
                .orElseThrow(()->new CustomException(ErrorCode.WEEKLY_ANALYSIS_NOT_FOUND));
        //기존 완료 리스트 가져오기
        List<String> completed=analysis.getCompletedChapters();
        if(!completed.contains(chapterId)){
            completed.add(chapterId);
        }

        analysis.setCompletedChapters(completed);
        weeklyAnalysisRepository.save(analysis);
    }
}
