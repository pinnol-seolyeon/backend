package jpabasic.pinnolbe.service.question;

import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.question.QuestionSessionDto;
import jpabasic.pinnolbe.dto.question.QuestionRequest;
import jpabasic.pinnolbe.dto.question.QuestionResponse;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.repository.question.QueCollectionRepository;
import jpabasic.pinnolbe.service.model.AskQuestionTemplate;
import org.springframework.stereotype.Service;
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

    //사용자별 세션 저장소 //메모리에 저장된 질문 세션 관리 -> 일시적으로 관리
    private final Map<String, QuestionSessionDto> sessionStore = new ConcurrentHashMap<>();


    public QuestionService(QueCollectionRepository queCollectionRepository, AskQuestionTemplate askQuestionTemplate,
                           WeeklyAnalysisRepository weeklyAnalysisRepository, QuestionAnalyzer questionAnalyzer) {
        this.queCollectionRepository = queCollectionRepository;
        this.askQuestionTemplate = askQuestionTemplate;
        this.weeklyAnalysisRepository = weeklyAnalysisRepository;
        this.questionAnalyzer = questionAnalyzer;
    }


    //질문 내용을 AI 모델에게 전달
    public QuestionResponse askQuestion(String question, User user) {
        String userId = user.getId();
        QuestionRequest request = new QuestionRequest(null,userId, question);

        try {
            QuestionResponse result = askQuestionTemplate.askQuestionToAI(request);
            String answer = result.getResult();

            //세션에 저장
            saveQuestionSession(question, answer,userId);
            return result;
        } catch (RestClientException e) {
            throw new RuntimeException("AI 서버 호출 중 오류 발생", e);
        }
    }

    //질문 내용 session에 저장
    public void saveQuestionSession(String question,String answer,String userId) {
        //사용자 세션 가져오기
        QuestionSessionDto session = sessionStore.computeIfAbsent(userId, k -> new QuestionSessionDto());
        session.add(question, answer);
        System.out.println("QuestionSession:" + session);
    }


    //질문에 따른 표현력 점수 측정 (3단계 학습 완료 시)
    public double getExpressionScore(List<String> question) {
        double score=QuestionAnalyzer.calculateScores(question);
        //질문에 따른 표현력 점수 측정
        System.out.println("✔️표현력 점수:"+score);
        return QuestionAnalyzer.calculateScores(question);
    }


    //모든 질문+답변 한꺼번에 DB에 저장하기
    public List<String> saveAllQAs(User user, String chapterId) {
        String userId = user.getId();
        QuestionSessionDto session = sessionStore.get(userId);

        if (session == null || session.getQuestions().isEmpty()) return null;

        QueCollection doc = new QueCollection();
        doc.setUserId(userId);
        doc.setQuestions(session.getQuestions());
        doc.setAnswers(session.getAnswers());
        doc.setChapterId(chapterId);
//        LocalDateTime nowKST=LocalDateTime.now(ZoneId.of("Asia/Seoul"));
//        doc.setDate(nowKST);

        queCollectionRepository.save(doc);

        //저장 후 세션 초기화  //sessionStore에서 key가 userId인 entry하나만 삭제
        sessionStore.remove(userId);

        List<String> questions = doc.getQuestions();
        return questions;
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
        List<String> completed = analysis.getCompletedChapters();
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



//    //단원별로 질문 모아두는 레포 생성
//    public QueCollection makeQueCollectionRepo(int part, User user){
//
//        QueCollection queCollection=new QueCollection(part,user);
//        queCollection.setDate(new Date());
//
//        queCollectionRepository.save(queCollection);
//        return queCollection;
//
//    }
}
