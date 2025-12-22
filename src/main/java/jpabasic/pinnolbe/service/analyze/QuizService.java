package jpabasic.pinnolbe.service.analyze;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.domain.analyze.quiz.QuizNotes;
import jpabasic.pinnolbe.domain.redis.StudySession;
import jpabasic.pinnolbe.dto.quiz.QuizAnalyzeDto;
import jpabasic.pinnolbe.dto.quiz.QuizType;
import jpabasic.pinnolbe.dto.quiz.SolvedQuizResDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.repository.analyze.quiz.QuizNotesRepository;
import jpabasic.pinnolbe.service.facade.SessionFacade;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final UserService userService;
    private final WeeklyAnalysisRepository weeklyAnalysisRepository;
    private final QuizNotesRepository quizNotesRepository;
    private final SessionFacade sessionFacade;


    public void upsertUnderstanding(List<QuizAnalyzeDto> results) {
        User user = userService.getUserInfo();
        String userId = user.getId();

        LocalDate weekStart = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(DayOfWeek.MONDAY);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        // --- 1) 새 호출에서 계산된 값 ---
        int newTotal = results.size();
        int newCorrect = (int) results.stream()
                .filter(r -> r.getUserAnswer() != null
                        && r.getUserAnswer().equals(r.getCorrectAnswer()))
                .count();

        // --- 2) 기존 문서 조회 ---
        List<WeeklyAnalysis> list = weeklyAnalysisRepository
                .findAllByUserIdAndWeekStartDate(userId, weekStart);

        WeeklyAnalysis analysis;
        if (list.isEmpty()) {
            // 최초 생성: 이해도·집중도 초기값 세팅
            analysis = WeeklyAnalysis.builder()
                    .userId(userId)
                    .weekStartDate(weekStart)
                    .understandingData(
                            WeeklyAnalysis.UnderstandingData.builder()
                                    .correct(newCorrect)
                                    .total(newTotal)
                                    .build()
                    )
                    .analyzedAt(now)
                    .build();
        } else {
            analysis = list.get(0);

            // --- 이해도 누적 ---
            int prevCorrect = Optional.ofNullable(analysis.getUnderstandingData())
                    .map(WeeklyAnalysis.UnderstandingData::getCorrect)
                    .orElse(0);
            int prevTotal = Optional.ofNullable(analysis.getUnderstandingData())
                    .map(WeeklyAnalysis.UnderstandingData::getTotal)
                    .orElse(0);

            analysis.setUnderstandingData(
                    WeeklyAnalysis.UnderstandingData.builder()
                            .correct(prevCorrect + newCorrect)
                            .total(prevTotal + newTotal)
                            .build()
            );

            analysis.setAnalyzedAt(now);
        }

        // --- 3) upsert (생성 또는 업데이트) ---
        weeklyAnalysisRepository.save(analysis);
    }

    /// 틀린문제들 DB에 저장
    public List<QuizNotes.QuizRecord> saveQuizzes(List<QuizAnalyzeDto> results, QuizType quizType) {
        User user = userService.getUserInfo();
        String userId=user.getId();
        //redis session에서 유저 현 진도 불러오기
        StudySession session = sessionFacade.getSessionByUser(user);
        //현재 학습 중인 chapterId
        String chapterId = session.getChapterId();

        //quizNotes 중복 저장 방지 로직
        QuizNotes existing=quizNotesRepository
            .findByUserIdAndChapterIdAndQuizType(userId,chapterId,quizType)
            .orElse(null);

        //quizRecord 객체 생성 후 모든 문제 저장, 리스트 반환
        List<QuizNotes.QuizRecord> quizzes = results.stream()
                .map(r -> new QuizNotes.QuizRecord(r.getQuizId(), r.getQuestion(), r.getUserAnswer(), r.getCorrectAnswer(),r.getIsCorrect(),r.getDescription(),r.getOptions()))
                .toList();

        QuizNotes quizNotes;
        if (existing == null) {
            quizNotes=new QuizNotes(userId,chapterId,quizzes,quizType);
            quizNotesRepository.save(quizNotes);
        }else{
            return quizzes;
        }

        return quizzes;
    }


    /// 복습하기 틀린문제들 DB에 저장
    public List<QuizNotes.QuizRecord> saveReviewQuizzes(List<QuizAnalyzeDto> results,String chapterId, QuizType quizType) {
        User user = userService.getUserInfo();

        //quizRecord 객체 생성 후 모든 문제 저장, 리스트 반환
        List<QuizNotes.QuizRecord> quizes = results.stream()
                .map(r -> new QuizNotes.QuizRecord(r.getQuizId(), r.getQuestion(), r.getUserAnswer(), r.getCorrectAnswer(),r.getIsCorrect(),r.getDescription(),r.getOptions()))
                .toList();

        //quizNote 객체 생성 후 저장
        QuizNotes quizNotes = new QuizNotes(user.getId(), chapterId, quizes,quizType);
        quizNotesRepository.save(quizNotes);

        return quizes;
    }


    /// 내가 푼 문제 상세 페이지 조회
    public SolvedQuizResDto getSolvedQuizDetails(String chapterId) {
        List<QuizNotes.QuizRecord> quizRecords = getQuizList(chapterId);

        double correctRate = getQuizCorrectRate(quizRecords);
        return new SolvedQuizResDto(quizRecords, correctRate);
    }

    /// 풀이한 문제들 조회
    private List<QuizNotes.QuizRecord> getQuizList(String chapterId) {
        User user = userService.getUserInfo();
        QuizNotes notes = quizNotesRepository.findByUserIdAndChapterIdAndQuizType(user.getId(), chapterId,QuizType.MAIN_STUDY)
                .orElseThrow(() -> new CustomException(ErrorCode.QUIZ_NOTES_NOT_FOUND));

        List<QuizNotes.QuizRecord> result=notes.getRecords();
        System.out.println("⭐ quizResult:"+result.stream().toList());
        if (result.isEmpty()) {
            return null;
        }
        return result;
    }

    /// 퀴즈 정답률 계산
    private double getQuizCorrectRate(List<QuizNotes.QuizRecord> quizRecords) {

        if (quizRecords == null || quizRecords.isEmpty()) {
            // 문제를 하나도 안 풀었을 때
            throw new CustomException(ErrorCode.QUIZ_NOT_SOLVED_YET);
        }

        long wrongNumbers = quizRecords.stream()
                .filter(q -> !q.getIsCorrect())
                .count();
        int total = quizRecords.size();
        return 100.0-(((double) wrongNumbers / total) * 100.0);
    }

}
