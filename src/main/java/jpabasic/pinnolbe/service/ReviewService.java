package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.ChapterProgress;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.domain.analyze.quiz.QuizNotes;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.dto.review.*;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.ChapterProgressRepository;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.repository.analyze.quiz.QuizNotesRepository;
import jpabasic.pinnolbe.service.analyze.WeeklyAnalysisService;
import jpabasic.pinnolbe.service.model.ReviewAITemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
public class ReviewService {

    private final ChapterService chapterService;
    private final QuizNotesRepository quizNotesRepository;
    private final ReviewAITemplate reviewAITemplate;
    private final BookService bookService;
    private final WeeklyAnalysisService weeklyAnalysisService;
    private final WeeklyAnalysisRepository weeklyAnalysisRepository;
    private final ChapterProgressRepository chapterProgressRepository;

    public ReviewService(ChapterService chapterService, QuizNotesRepository quizNotesRepository, ReviewAITemplate reviewAITemplate, BookService bookService, WeeklyAnalysisService weeklyAnalysisService, WeeklyAnalysisRepository weeklyAnalysisRepository, ChapterProgressRepository chapterProgressRepository) {
        this.chapterService = chapterService;
        this.quizNotesRepository = quizNotesRepository;
        this.reviewAITemplate = reviewAITemplate;
        this.bookService = bookService;
        this.weeklyAnalysisService = weeklyAnalysisService;
        this.weeklyAnalysisRepository = weeklyAnalysisRepository;
        this.chapterProgressRepository = chapterProgressRepository;
    }

    /**
     * 새로운 복습 퀴즈 생성
     * @param userId
     * @param chapterId
     * @return
     */
    public ReviewQuizResDto restructureContent(String userId, String chapterId){
        ReviewReqDto request=buildRequestDto(userId, chapterId);
        //AI를 통한 리뷰 퀴즈 생성
        ReviewQuizResDto result=reviewAITemplate.makeReviewQuizByAI(request);
        return result;
    }

    /**
     * 새로운 복습 텍스트 생성
     * @param userId
     * @param chapterId
     * @return
     */
    public TextReviewResDto createTextReview(String userId, String chapterId){
        ReviewReqDto request=buildRequestDto(userId, chapterId);
        //AI를 통한 텍스트 리뷰 생성
        TextReviewResDto result=reviewAITemplate.makeTextReviewByAI(request);
        return result;
    }

    /**
     * 복습하기 로직 구현(날짜별)
     * @param user
     * @param chapterId
     * @return
     */
    public ReviewLockStatus getReviewStatus(User user, String chapterId){
        String userId=user.getId();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        //ChapterProgress 조회
        ChapterProgress progress=chapterProgressRepository
                .findByUserIdAndChapterId(userId, chapterId)
                .orElse(null);

        //아직 학습 자체를 안했으면 둘다 locked
        if(progress==null){
            return new ReviewLockStatus(ReviewStatus.LOCKED,ReviewStatus.LOCKED);
        }

        ReviewStatus firstStatus;
        //1차 복습 : 이미 완료한 경우
       if(progress.getFirstReviewCompletedAt()!=null){
           firstStatus=ReviewStatus.COMPLETED;
       } else if (!today.isBefore((progress.getCompletedAt().plusDays(3)))) {
           firstStatus=ReviewStatus.UNLOCKED;
       }else{
           firstStatus=ReviewStatus.LOCKED;
       }

        //2차 복습 잠금 해제 조건 : 1차 복습 + 4일
        ReviewStatus secondStatus;

        // 2-1) 이미 완료한 경우
        if (progress.getSecondReviewCompletedAt() != null) {
            secondStatus = ReviewStatus.COMPLETED;

            // 2-2) 1차 복습 자체를 안 했으면 -> 무조건 LOCKED
        } else if (progress.getFirstReviewCompletedAt() == null) {
            secondStatus = ReviewStatus.LOCKED;

        } else {
            // 2-3) 날짜 조건: 1차 복습 완료 + 4일
            LocalDate secondUnlockDate = progress.getFirstReviewCompletedAt().plusDays(4);

            if (!today.isBefore(secondUnlockDate)) {
                secondStatus = ReviewStatus.UNLOCKED;
            } else {
                secondStatus = ReviewStatus.LOCKED;
            }
        }

        return new ReviewLockStatus(firstStatus,secondStatus);
    }

    //복습하기 페이지 화면에 반환할 내용들
    public List<ReviewListResDto> getReviewList(User user){
        List<ChapterProgress> chapterProgress=chapterProgressRepository
                .findByUserId(user.getId());
        if(chapterProgress.isEmpty()){
            return null;
        }

        chapterProgress.stream()
                .map(p->getReviewStatus(user,p.getChapterId()));
    }


    private ReviewReqDto buildRequestDto(String userId, String chapterId){
        Chapter chapter = chapterService.findChapter(chapterId);
        String bookId=chapter.getBookId();

        int bookLevel=bookService.getBooklevel(bookId);

        QuizNotes notes = quizNotesRepository.findByUserIdAndChapterId(userId, chapterId)
                .orElseThrow(() -> new CustomException(ErrorCode.QUIZ_NOTES_NOT_FOUND));

        return new ReviewReqDto(
                userId,
                bookLevel,
                chapter.getOrder(),
                notes.getRecords()
        );
    }


}
