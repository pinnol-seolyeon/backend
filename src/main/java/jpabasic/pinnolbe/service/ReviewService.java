package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.ChapterProgress;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.quiz.QuizNotes;
import jpabasic.pinnolbe.domain.badge.BadgeType;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.dto.badge.BadgeRequestDto;
import jpabasic.pinnolbe.dto.quiz.QuizType;
import jpabasic.pinnolbe.dto.review.*;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.ChapterProgressRepository;
import jpabasic.pinnolbe.repository.analyze.quiz.QuizNotesRepository;
import jpabasic.pinnolbe.service.model.ReviewAITemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReviewService {

    private final ChapterService chapterService;
    private final QuizNotesRepository quizNotesRepository;
    private final ReviewAITemplate reviewAITemplate;
    private final BookService bookService;
    private final ChapterProgressRepository chapterProgressRepository;
    private final BadgeService badgeService;

    public ReviewService(ChapterService chapterService, QuizNotesRepository quizNotesRepository, ReviewAITemplate reviewAITemplate, BookService bookService, ChapterProgressRepository chapterProgressRepository, BadgeService badgeService) {
        this.chapterService = chapterService;
        this.quizNotesRepository = quizNotesRepository;
        this.reviewAITemplate = reviewAITemplate;
        this.bookService = bookService;
        this.chapterProgressRepository = chapterProgressRepository;
        this.badgeService = badgeService;
    }

    /**
     * 새로운 복습 퀴즈 생성
     * @param userId
     * @param chapterId
     * @return
     */
    public ReviewQuizResDto createQuizReview(int reviewCount, String userId, String chapterId){
        QuizNotes notes=findQuizNotes(chapterId,reviewCount,userId);
        List<QuizNotes.QuizRecord> records=notes.getRecords();
        ReviewReqDto request=buildRequestDto(userId, chapterId,records);
        //AI를 통한 리뷰 퀴즈 생성
        ReviewQuizResDto result=reviewAITemplate.makeReviewQuizByAI(request);
        return result;
    }

    /**
     * 퀴즈 내역 찾기
     */
    private QuizNotes findQuizNotes(String chapterId,int reviewCount,String userId) {
        QuizNotes notes;
        if(reviewCount==1){
            notes = quizNotesRepository.findByUserIdAndChapterIdAndQuizType(userId, chapterId, QuizType.MAIN_STUDY)
                    .orElseThrow(() -> new CustomException(ErrorCode.QUIZ_NOTES_NOT_FOUND));
        }else{
            notes = quizNotesRepository.findByUserIdAndChapterIdAndQuizType(userId, chapterId, QuizType.FIRST_REVIEW)
                    .orElseThrow(() -> new CustomException(ErrorCode.QUIZ_NOTES_NOT_FOUND));
        }
        return notes;
    }

    /**
     * 새로운 복습 텍스트 생성
     * @param userId
     * @param chapterId
     * @return
     */
    public TextReviewResDto createTextReview(int reviewCount,String userId, String chapterId){
        QuizNotes notes=findQuizNotes(chapterId,reviewCount,userId);
        List<QuizNotes.QuizRecord> records=notes.getRecords();
        ReviewReqDto request=buildRequestDto(userId, chapterId,records);
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
    public Page<ReviewListResDto> getReviewList(User user,int page) {
        String userId = user.getId();
        Pageable pageable = PageRequest.of(page, 5);

        //DB에서 chapterProgress 같은 단위로 가져옴
        Page<ChapterProgress> progressPage =
                chapterProgressRepository.findByUserId(userId, pageable);
        //조회된 chapterId 목록
        List<String> chapterIds = progressPage.getContent().stream()
                .map(ChapterProgress::getChapterId)
                .toList();
        //titleMap으로 한 번에 가져오기 (N+1 방지)
        Map<String, String> titles = chapterService.findChapterTitle(chapterIds);
        ;
        //Page.map() 이용 -> paging 정보를 유지한 상태로 dto 변환
        return progressPage.map(p ->
                new ReviewListResDto(
                        p.getChapterId(),
                        titles.get(p.getChapterId()),
                        getReviewStatus(user, p.getChapterId())
                ));

    }


    private ReviewReqDto buildRequestDto(String userId, String chapterId,List<QuizNotes.QuizRecord> records){
        Chapter chapter = chapterService.findChapter(chapterId);
        String bookId=chapter.getBookId();

        int bookLevel=bookService.getBooklevel(bookId);
        return new ReviewReqDto(
                userId,
                bookLevel,
                chapter.getOrder(),
                records
        );
    }

    /**
     * 1/2차 복습 완료 로직
     */
    @Transactional
    public void completeReview(String userId,int reviewCount,String chapterId){
        LocalDate today=LocalDate.now(ZoneId.of("Asia/Seoul"));
        ChapterProgress progress=chapterProgressRepository
                .findByUserIdAndChapterId(userId, chapterId)
                .orElseThrow(()->new CustomException(ErrorCode.CHAPTER_PROGRESS_NOT_FOUND));

        // 1차 완료 중복 체크
        if (reviewCount == 1 && progress.getFirstReviewCompletedAt() != null) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_COMPLETED);
        }

        // 2차는 1차 완료 여부 체크
        if (reviewCount == 2) {
            if (progress.getFirstReviewCompletedAt() == null) {
                throw new CustomException(ErrorCode.FIRST_REVIEW_NOT_COMPLETED);
            }
            if (progress.getSecondReviewCompletedAt() != null) {
                throw new CustomException(ErrorCode.REVIEW_ALREADY_COMPLETED);
            }
        }

        //복습 회차 별 로직 설계
        if(reviewCount==1){
            progress.setFirstReviewCompletedAt(today);
            chapterProgressRepository.save(progress);
        }else{
            //모든 복습 완료 시 chapterProgress(복습 진도 저장 도메인) 삭제
            chapterProgressRepository.delete(progress);
            //Badge 획득
            badgeService.getBadge(new BadgeRequestDto(chapterId,BadgeType.MODEL_STUDENT),userId);
        }

    }


}
