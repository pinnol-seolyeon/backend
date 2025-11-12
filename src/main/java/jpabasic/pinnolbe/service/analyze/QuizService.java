package jpabasic.pinnolbe.service.analyze;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.domain.analyze.quiz.QuizNotes;
import jpabasic.pinnolbe.domain.analyze.quiz.QuizRecord;
import jpabasic.pinnolbe.domain.study.Quiz;
import jpabasic.pinnolbe.dto.analyze.QuizAnalyzeDto;
import jpabasic.pinnolbe.repository.analyze.QuizNotesRepository;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.repository.study.QuizRepository;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final UserService userService;
    private final WeeklyAnalysisRepository weeklyAnalysisRepository;
    private final QuizNotesRepository quizNotesRepository;

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
    public void saveWrongQuizes(List<QuizAnalyzeDto> results) {
        User user = userService.getUserInfo();
        user.getStudySessionLogId();
        QuizNotes quizNotes=new QuizNotes();
        List<QuizRecord> wrongs=results.stream()
                .filter(r->!r.getIsCorrect()) //isCorrect==false인 객체만 필터링
                .map(r->new QuizRecord(r.getQuizId(),r.getUserAnswer(),r.getCorrectAnswer()))
                .toList();


//        quizNotesRepository.save(wrongs);
    }
}
