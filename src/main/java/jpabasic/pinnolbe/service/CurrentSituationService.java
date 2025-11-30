package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.CurrentSituationStatus;
import jpabasic.pinnolbe.domain.Status;
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

        //현재 studySessionLog 찾기
        StudySessionLog log = studyLogService.findStudySessionLog(sessionId);

        //현재 진도 찾기
        String currentChapterId=log.getChapterId();
        int currentLevel=log.getLevel();
        //현재 학습 중인 chapter
        Chapter chapter=chapterService.findChapter(currentChapterId);
        Boolean isCurrent; //true=현재 학습중인 단원, false=이미 완료한 단원(아직 다음단원 학습X)

        //status==NOT_STARTED, level==1 -> 아직 해당 단원 학습 시작 안함
        if(log.getStatus()== Status.NOT_STARTED && log.getLevel()==1){
            System.out.println("🚨 현재 단원이 학습중인 단원이 아님..! 그러므로 이전 학습완료된 단원 호출");
            int completedChapterOrder=chapter.getOrder()-1;
            //여태까지 학습 완료한 마지막 chapter(위에서 구한 chapter에 대한 학습을 아직 시작하지 않음)
            chapter=chapterService.findChapterByOrder(log.getBookId(),completedChapterOrder);
            System.out.println("🚨 학습완료한 단원:"+chapter.getOrder());
            isCurrent=false;
        } else {
			isCurrent = true;
		}

		String currentBookId=chapter.getBookId();
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
                if(!isCurrent){//isCurrent=false,현재 학습중인 단원X
                    status=CurrentSituationStatus.COMPLETED;
                    progress=100.0;
                }else{
                    status = CurrentSituationStatus.STUDYING;
                    progress = getChapterProgress(currentLevel);
                }
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

