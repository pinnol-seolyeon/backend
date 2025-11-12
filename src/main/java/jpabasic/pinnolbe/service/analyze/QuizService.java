package jpabasic.pinnolbe.service.analyze;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.domain.analyze.quiz.QuizNotes;
import jpabasic.pinnolbe.domain.analyze.quiz.QuizRecord;
import jpabasic.pinnolbe.domain.redis.StudySession;
import jpabasic.pinnolbe.domain.study.Quiz;
import jpabasic.pinnolbe.dto.analyze.QuizAnalyzeDto;
import jpabasic.pinnolbe.dto.analyze.QuizRecordDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.analyze.quiz.QuizNotesRepository;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.repository.analyze.quiz.QuizRecordRepository;
import jpabasic.pinnolbe.repository.study.QuizRepository;
import jpabasic.pinnolbe.service.login.UserService;
import jpabasic.pinnolbe.service.study.StudySessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final UserService userService;
    private final WeeklyAnalysisRepository weeklyAnalysisRepository;
    private final QuizNotesRepository quizNotesRepository;
    private final StudySessionService studySessionService;
    private final QuizRecordRepository quizRecordRepository;

    public List<Quiz> getQuiz(String chapterId, int limit) {
        List<Quiz> all = quizRepository.findByChapterId(chapterId);
        System.out.println("✅ 찾은 퀴즈 개수: " + (all == null ? "null" : all.size()));
        if (all == null) return new ArrayList<>();
        Collections.shuffle(all);
        return all.stream().limit(limit).collect(Collectors.toList());
    }

    public void upsertUnderstanding(List<QuizAnalyzeDto> results) {
        User user = userService.getUserInfo();
        String userId = user.getId();

        LocalDate weekStart = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(DayOfWeek.MONDAY);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        // --- 1) 새 호출에서 계산된 값 ---
        int newTotal   = results.size();
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
            int prevTotal   = Optional.ofNullable(analysis.getUnderstandingData())
                    .map(WeeklyAnalysis.UnderstandingData::getTotal)
                    .orElse(0);

            analysis.setUnderstandingData(
                    WeeklyAnalysis.UnderstandingData.builder()
                            .correct(prevCorrect + newCorrect)
                            .total(prevTotal   + newTotal)
                            .build()
            );

            analysis.setAnalyzedAt(now);
        }

        // --- 3) upsert (생성 또는 업데이트) ---
        weeklyAnalysisRepository.save(analysis);
    }

    /// 틀린문제들 DB에 저장
    public List<QuizRecord> saveWrongQuizes(List<QuizAnalyzeDto> results) {
        User user = userService.getUserInfo();
        //redis session에서 유저 현 진도 불러오기
        StudySession session=studySessionService.getSessionByUser(user);
        //현재 학습 중인 chapterId
        String chapterId=session.getChapterId();

        //quizNote 객체 생성 후 저장
        QuizNotes quizNotes=new QuizNotes(user.getId(),chapterId);
        QuizNotes saved=quizNotesRepository.save(quizNotes);
        String quizNotesId=saved.getId();

        //틀린 문제 각각 저장
        List<QuizRecord> wrongs=results.stream()
                .filter(r->!r.getIsCorrect()) //isCorrect==false인 객체만 필터링
                .map(r->new QuizRecord(quizNotesId,r.getQuizId(),r.getUserAnswer(),r.getCorrectAnswer()))
                .toList();
        quizRecordRepository.saveAll(wrongs);

        return wrongs;
    }

    /// 틀린 문제들 조회
    public QuizRecordDto getWrongQuizes(String chapterId){
        User user = userService.getUserInfo();
        QuizNotes notes=quizNotesRepository.findByUserIdAndChapterId(user.getId(),chapterId)
                .orElseThrow(()->new CustomException(ErrorCode.QUIZ_NOTES_NOT_FOUND));
        String quizNotesId=notes.getId();

        List<QuizRecord> records=quizRecordRepository.findAllByQuizNotesId(quizNotesId);

        List<String> quizIds=records.stream()
                .map(QuizRecord::getQuizId)
                .toList();
        //quizId -> question 매핑
        Map<String,String> quizMap=quizRepository.findAllById(quizIds).stream()
                .collect(Collectors.toMap(Quiz::getId,Quiz::getQuiz));
        List<QuizRecordDto.EachQuiz> eachQuizes=records.stream()
                .map(record->new QuizRecordDto.EachQuiz(
                        quizMap.getOrDefault(record.getQuizId(),"질문 없음"),
                        record
                ))
                .toList();

        return new QuizRecordDto(chapterId,eachQuizes);

    }
}
