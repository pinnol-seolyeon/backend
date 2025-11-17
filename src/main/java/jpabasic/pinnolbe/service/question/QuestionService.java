package jpabasic.pinnolbe.service.question;

import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.domain.analyze.quiz.QuizNotes;
import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.redis.StudySession;
import jpabasic.pinnolbe.domain.study.Book;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.dto.question.QuestionRequest;
import jpabasic.pinnolbe.dto.question.QuestionTempCache;
import jpabasic.pinnolbe.dto.review.ReviewReqDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.repository.question.QueCollectionRepository;
import jpabasic.pinnolbe.service.BookService;
import jpabasic.pinnolbe.service.ChapterService;
import jpabasic.pinnolbe.service.model.AskQuestionTemplate;
import jpabasic.pinnolbe.service.study.StudySessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuestionService {

    private final QueCollectionRepository queCollectionRepository;
    private final AskQuestionTemplate askQuestionTemplate;
    private final WeeklyAnalysisRepository weeklyAnalysisRepository;
    private final QuestionAnalyzer questionAnalyzer;
    private final QuestionTempCache tempCache;

    //사용자별 세션 저장소 //메모리에 저장된 질문 세션 관리 -> 일시적으로 관리
    private final Map<String, QuestionTempCache> sessionStore = new ConcurrentHashMap<>();
    private final StudySessionService studySessionService;
    private final BookService bookService;
    private final ChapterService chapterService;


    public QuestionService(QueCollectionRepository queCollectionRepository, AskQuestionTemplate askQuestionTemplate,
                           WeeklyAnalysisRepository weeklyAnalysisRepository, QuestionAnalyzer questionAnalyzer,
                           QuestionTempCache tempCache, StudySessionService studySessionService, BookService bookService, ChapterService chapterService) {
        this.queCollectionRepository = queCollectionRepository;
        this.askQuestionTemplate = askQuestionTemplate;
        this.weeklyAnalysisRepository = weeklyAnalysisRepository;
        this.questionAnalyzer = questionAnalyzer;
        this.tempCache = tempCache;
        this.studySessionService = studySessionService;
        this.bookService = bookService;
        this.chapterService = chapterService;
    }



    //질문에 따른 표현력 점수 측정 (3단계 학습 완료 시)
    public double getExpressionScore(List<String> question) {
        double score=QuestionAnalyzer.calculateScores(question);
        //질문에 따른 표현력 점수 측정
        System.out.println("✔️표현력 점수:"+score);
        return QuestionAnalyzer.calculateScores(question);
    }


    public QuestionRequest askQuestion(String question,User user){
        StudySession session=studySessionService.getSessionByUser(user);

        String chapterId=session.getChapterId();
        String bookId=session.getBookId();

        int level=bookService.getBooklevel(bookId); //책
        int order=chapterService.findChapter(chapterId).getOrder(); //단원(chapter)

        return new QuestionRequest(user.getId(),question,level,order);
    }

    @Transactional
    public List<String> commitUserSession(String userId,String chapterId) {
        List<QuestionTempCache.TempQA> allQAs = tempCache.popAll(userId);
        List<String> questionList=new ArrayList<>();

        if (allQAs == null || allQAs.isEmpty()) {
            return Collections.singletonList("저장할 세션이 없습니다.");
        }

        for(QuestionTempCache.TempQA tempQA : allQAs) {
            //saveQuestionSession이 DB에 append하면서 최신 질문 리스트 반환한다
            List<String> savedQuestions=saveQuestionSession(
                    tempQA.getQuestion(),
                    tempQA.getAnswer(),
                    userId,
                    chapterId
            );
            questionList.addAll(savedQuestions);
        }

        return questionList;
    }

    public List<String> saveQuestionSession(String question, String answer, String userId,String chapterId) {
        // 오늘 날짜 (시간은 버리고 일자 단위로)
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // ✅ 기존 세션(오늘자)이 존재하면 append, 없으면 새로 생성
        QueCollection queCollection = queCollectionRepository
                .findByUserIdAndDate(userId, today)
                .orElseGet(() -> {
                    QueCollection newSession = new QueCollection();
                    newSession.setUserId(userId);
                    newSession.setDate(today);
                    newSession.setChapterId(chapterId);
                    newSession.setQuestions(new ArrayList<>());
                    newSession.setAnswers(new ArrayList<>());
                    return newSession;
                });

        // ✅ 새 질문/답변 추가
        if (queCollection.getQuestions() == null) {
            queCollection.setQuestions(new ArrayList<>());
        }
        if (queCollection.getAnswers() == null) {
            queCollection.setAnswers(new ArrayList<>());
        }

        queCollection.getQuestions().add(question);
        queCollection.getAnswers().add(answer);

        // ✅ MongoDB에 저장 (insert or update)
        QueCollection saved=queCollectionRepository.save(queCollection);
        return new ArrayList<>(saved.getQuestions());
    }


    // 참여도(질문 개수)
    public void updateWeeklyQuestionCount(User user) {
        String userId = user.getId();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDateTime startDt = weekStart.atStartOfDay();
        LocalDateTime endDt = weekStart.plusDays(7).atStartOfDay();

        // 1) 이번 주에 저장된 모든 세션 문서 조회
        List<QueCollection> sessions =
                queCollectionRepository.findAllByUserIdAndCreatedAtBetween(userId, startDt, endDt);

        System.out.println("조회된 세션 개수: " + sessions.size());
        for (QueCollection qc : sessions) {
            System.out.println("📘 ID: " + qc.getId());
            System.out.println("📅 Date: " + qc.getDate());
            System.out.println("❓ Questions: " + qc.getQuestions());
        }
        System.out.println("쿼리 범위: " + startDt + " ~ " + endDt);

        // 2) 각 세션의 질문 개수를 합산
        int totalQcount = sessions.stream()
                .mapToInt(qc -> qc.getQuestions() == null ? 0 : qc.getQuestions().size())
                .sum();
        System.out.println("✔️이번주 질문개수:" + totalQcount);

        // 3) 같은 주차의 WeeklyAnalysis 조회
        WeeklyAnalysis analysis =
                weeklyAnalysisRepository.findByUserIdAndWeekStartDate(user.getId(), weekStart)
                        .orElseGet(() -> new WeeklyAnalysis(user.getId(), weekStart));
        WeeklyAnalysis.EngagementData engagementData;
        if (analysis.getEngagementData() == null) {
            engagementData = new WeeklyAnalysis.EngagementData();
            analysis.setEngagementData(engagementData);
        } else {
            engagementData = analysis.getEngagementData();
            //기존에 저장되어있던 질문 개수에 더해준다
            totalQcount += engagementData.getQuestionCount();
        }
        //questionCount 업데이트
        engagementData.setQuestionCount(totalQcount);
        //변경 시간 업데이트
        analysis.setAnalyzedAt(LocalDateTime.now());
        System.out.println("🧩 저장 전 ID: " + analysis.getId());
        //저장
        weeklyAnalysisRepository.save(analysis);
    }



    // 표현력
    public void updateExpressionScore(User user,List<String> questions) {
        String userId = user.getId();
        LocalDate weekStart = LocalDate.now(ZoneId.of("Asia/Seoul")).with(DayOfWeek.MONDAY);

        //표현력 점수 측정
        double newScore=getExpressionScore(questions);

        WeeklyAnalysis analysis = weeklyAnalysisRepository
                .findByUserIdAndWeekStartDate(userId, weekStart)
                .orElseGet(() -> WeeklyAnalysis.builder()
                        .userId(userId)
                        .weekStartDate(weekStart)
                        .expressionData(new WeeklyAnalysis.ExpressionData())
                        .analyzedAt(LocalDateTime.now())
                        .build());

        //expressionData 초기화
        if (analysis.getExpressionData() == null) {
            analysis.setExpressionData(new WeeklyAnalysis.ExpressionData());
        }
        WeeklyAnalysis.ExpressionData expr = analysis.getExpressionData();

        //이전 점수 가져오기
        double prevScore=expr.getExpressionScore();

        //completedChapters가 null일 경우 대비
        List<WeeklyAnalysis.CompletedChapter> completed = analysis.getCompletedChapters();
        int completedSize = (completed != null) ? completed.size() : 0;

        //평균 계산 : (이전 평균*완료 단원 수+새 점수)/(완료 단원 수 +1)
        double updatedScore;
        if(completedSize==0){
            updatedScore=newScore;
        }else{
            updatedScore=(prevScore*completedSize+newScore)/(completedSize+1);
        }
        //저장
        expr.setExpressionScore(updatedScore);
        analysis.setExpressionData(expr);
        analysis.setAnalyzedAt(LocalDateTime.now());

        weeklyAnalysisRepository.save(analysis);
        System.out.println("✅ [" + userId + "] 이번주 표현력 점수 업데이트 완료: " + updatedScore);
    }

}
