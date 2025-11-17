package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.quiz.QuizNotes;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.dto.review.ReviewReqDto;
import jpabasic.pinnolbe.dto.review.ReviewQuizResDto;
import jpabasic.pinnolbe.dto.review.TextReviewResDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.analyze.quiz.QuizNotesRepository;
import jpabasic.pinnolbe.service.model.ReviewAITemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReviewService {

    private final ChapterService chapterService;
    private final QuizNotesRepository quizNotesRepository;
    private final ReviewAITemplate reviewAITemplate;
    private final BookService bookService;

    public ReviewService(ChapterService chapterService,QuizNotesRepository quizNotesRepository, ReviewAITemplate reviewAITemplate, BookService bookService) {
        this.chapterService = chapterService;
        this.quizNotesRepository = quizNotesRepository;
        this.reviewAITemplate = reviewAITemplate;
        this.bookService = bookService;
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

    public void getReviewList(User user){

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
