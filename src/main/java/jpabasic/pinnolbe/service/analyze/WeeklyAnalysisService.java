package jpabasic.pinnolbe.service.analyze;

import org.springframework.dao.DuplicateKeyException;
import jpabasic.pinnolbe.domain.ChapterProgress;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.ChapterProgressRepository;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
public class WeeklyAnalysisService {

    private final WeeklyAnalysisRepository weeklyAnalysisRepository;
    private final ChapterProgressRepository chapterProgressRepository;

    public WeeklyAnalysisService(WeeklyAnalysisRepository weeklyAnalysisRepository, ChapterProgressRepository chapterProgressRepository) {
        this.weeklyAnalysisRepository = weeklyAnalysisRepository;
        this.chapterProgressRepository = chapterProgressRepository;
    }

    public WeeklyAnalysis findThisWeekAnalysis(String userId) {
        LocalDate weekStart = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(DayOfWeek.MONDAY);

        return weeklyAnalysisRepository.findByUserIdAndWeekStartDate(userId, weekStart)
                .orElseGet(() -> new WeeklyAnalysis(userId, weekStart));
    }

    @Transactional
    public void saveCompletedChapters(User user, String chapterId,String weeklyAnalysisId){
        String userId=user.getId();
        LocalDate weekStart = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(DayOfWeek.MONDAY);
        LocalDate today=LocalDate.now(ZoneId.of("Asia/Seoul"));

        //chapterProgress는 그냥 저장 시도
        try{
            ChapterProgress progress=new ChapterProgress(chapterId,today,userId);
            chapterProgressRepository.save(progress);
        }catch(DuplicateKeyException e){
            //이미 userId+chapterId 조합이 존재하는 경우 -> 무시
            log.debug("ChapterProgress already exists. userId={}, chapterId={}",
                    user.getId(), chapterId);
        }

        //WeeklyAnalysis 조회
        WeeklyAnalysis analysis =
                weeklyAnalysisRepository.findByUserIdAndWeekStartDate(userId, weekStart)
                        .orElseGet(() -> new WeeklyAnalysis(userId, weekStart));

        //기존 완료 리스트 가져오기
        List<WeeklyAnalysis.CompletedChapter> completed=analysis.getCompletedChapters();

        //weeklyAnalysis에 완료 기록 추가
        boolean exists=completed.stream()
                        .anyMatch(c->c.getChapterId().equals(chapterId));
        if(!exists){
            //weeklyAnalysis에 완료한 단원 추가(CompletedChapters)
            analysis.getCompletedChapters().add(
                    new WeeklyAnalysis.CompletedChapter(chapterId,today)
            );
        }

        weeklyAnalysisRepository.save(analysis);
    }
}
