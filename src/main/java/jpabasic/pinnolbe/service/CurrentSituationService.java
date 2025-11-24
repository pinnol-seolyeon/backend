package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.CurrentSituationStatus;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.StudySessionLog;
import jpabasic.pinnolbe.domain.badge.Badge;
import jpabasic.pinnolbe.domain.badge.BadgeType;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.dto.currentSituation.CurrentSituationResDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.service.analyze.WeeklyAnalysisService;
import jpabasic.pinnolbe.service.study.StudyLogService;
import jpabasic.pinnolbe.service.study.StudyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CurrentSituationService {

    private final StudyLogService studyLogService;
    private final ChapterService chapterService;
    private final BadgeService badgeService;

    public CurrentSituationService(StudyLogService studyLogService, ChapterService chapterService, BadgeService badgeService) {
        this.studyLogService = studyLogService;
        this.chapterService = chapterService;
        this.badgeService = badgeService;
    }

    public Slice<CurrentSituationResDto.CurrentChapterRes> getCurrentSituation(User user, int page) {
        String sessionId = user.getStudySessionLogId();
        if (sessionId == null) {
            throw new CustomException(ErrorCode.CURRENT_SITUATION_NOT_FOUND);
        }

        StudySessionLog log = studyLogService.findStudySessionLog(sessionId);
        String currentBookId = log.getBookId();
        String currentChapterId = log.getChapterId(); //학습 중이거나 앞으로 학습 완료할 단원
        int currentLevel = log.getLevel();

        Chapter chapter = chapterService.findChapter(currentChapterId);
        int currentOrder = chapter.getOrder();

        //현재 학습 중인 교재의 모든 chapter List
        Slice<CurrentSituationResDto.CurrentChapterRes> chapters = chapterService.getAllChapters(currentBookId, page);
        chapters.getContent().forEach(ch -> {
            CurrentSituationStatus status;
            double progress;
            List<BadgeType> badges = null;

            //학습 상태
            if (ch.getOrder() < currentOrder) {
                status = CurrentSituationStatus.COMPLETED;
                badges = badgeService.getBadgeList(user, ch.getChapterId());
                progress = 100.0;
            } else if (ch.getOrder() == currentOrder) {
                status = CurrentSituationStatus.STUDYING;
                progress = getChapterProgress(currentLevel);
            } else {
                status = CurrentSituationStatus.NOT_STARTED;
                progress = 0.0;
            }

            ch.setBadgeType(badges);
            ch.setStatus(status);
            ch.setProgress(progress);
        });

        return chapters;
    }

    private Double getChapterProgress(int currentLevel) {
        return (currentLevel / 6.0) * 100;
    }

}

