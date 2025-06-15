package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.question.QAs;
import jpabasic.pinnolbe.dto.question.QuestionSessionDto;
import jpabasic.pinnolbe.dto.question.QuestionRequest;
import jpabasic.pinnolbe.dto.question.QuestionResponse;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.repository.question.QueCollectionRepository;
import jpabasic.pinnolbe.repository.question.QuestionRepository;
import jpabasic.pinnolbe.service.model.AskQuestionTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QuestionService {

    private final QueCollectionRepository queCollectionRepository;
    private final AskQuestionTemplate askQuestionTemplate;
    private final WeeklyAnalysisRepository weeklyAnalysisRepository;

    //사용자별 세션 저장소 //메모리에 저장된 질문 세션 관리 -> 일시적으로 관리
    private final Map<String,QuestionSessionDto> sessionStore=new ConcurrentHashMap<>();


    public QuestionService(QueCollectionRepository queCollectionRepository, AskQuestionTemplate askQuestionTemplate, WeeklyAnalysisRepository weeklyAnalysisRepository) {
        this.queCollectionRepository = queCollectionRepository;
        this.askQuestionTemplate = askQuestionTemplate;
        this.weeklyAnalysisRepository = weeklyAnalysisRepository;
    }


    //질문 내용을 AI 모델에게 전달
    public QuestionResponse askQuestion(QuestionRequest request, User user){
        String userId= user.getId();
        String question=request.getQuestion();

        // AI에 유저의 질문 전달
        try {
            QuestionResponse answer = askQuestionTemplate.askQuestionToAI(request);

            //사용자 세션 가져오기
            QuestionSessionDto session=sessionStore.computeIfAbsent(userId,k->new QuestionSessionDto());
            session.add(question,answer.getResult());

            // AI의 답변 내용을 반환
            return answer;
        }catch(RestClientException e){
            throw new RuntimeException("AI 서버 호출 중 오류 발생", e);
        }

    }




    //모든 질문+답변 한꺼번에 DB에 저장하기
    public void saveAllQAs(User user,String chapterId){
        String userId=user.getId();
        QuestionSessionDto session=sessionStore.get(userId);

        if(session==null||session.getQuestions().isEmpty()) return;

        QueCollection doc=new QueCollection();
        doc.setUserId(userId);
        doc.setQuestions(session.getQuestions());
        doc.setAnswers(session.getAnswers());
        doc.setChapterId(chapterId);
        LocalDateTime nowKST=LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        doc.setDate(nowKST);

        queCollectionRepository.save(doc);

        //저장 후 세션 초기화  //sessionStore에서 key가 userId인 entry하나만 삭제
        sessionStore.remove(userId);

    }

    // 참여도
    public void updateWeeklyQuestionCount(User user) {
        String userId = user.getId();
        LocalDate today     = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDateTime startDt = weekStart.atStartOfDay();
        LocalDateTime endDt   = weekStart.plusDays(7).atStartOfDay();

        // 1) 이번 주에 저장된 모든 세션 문서 조회
        List<QueCollection> sessions =
                queCollectionRepository.findAllByUserIdAndDateBetween(userId, startDt, endDt);

        // 2) 각 세션의 질문 개수를 합산
        int totalQuestions = sessions.stream()
                .mapToInt(qc -> qc.getQuestions() == null ? 0 : qc.getQuestions().size())
                .sum();

        // 3) 같은 주차의 WeeklyAnalysis 조회
        List<WeeklyAnalysis> analyses =
                weeklyAnalysisRepository.findAllByUserIdAndWeekStartDate(userId, weekStart);

        WeeklyAnalysis analysis;
        if (analyses.isEmpty()) {
            // — 없으면 새로 생성
            analysis = WeeklyAnalysis.builder()
                    .userId(userId)
                    .weekStartDate(weekStart)
                    .engagementData(
                            WeeklyAnalysis.EngagementData.builder()
                                    .questionCount(totalQuestions)
                                    .build()
                    )
                    .analyzedAt(LocalDateTime.now())
                    .build();
        } else {
            // — 있으면 덮어쓰기(upsert)
            analysis = analyses.get(0);
            analysis.getEngagementData().setQuestionCount(totalQuestions);
            analysis.setAnalyzedAt(LocalDateTime.now());
        }
        weeklyAnalysisRepository.save(analysis);
    }

    // 표현력
    public void updateExpressionScore(User user, int newStarScore) {
        String userId = user.getId();
        LocalDate weekStart = LocalDate.now(ZoneId.of("Asia/Seoul")).with(DayOfWeek.MONDAY);

        List<WeeklyAnalysis> analyses =
                weeklyAnalysisRepository.findAllByUserIdAndWeekStartDate(userId, weekStart);

        WeeklyAnalysis analysis;
        if (analyses.isEmpty()) {
            // 첫 별점이므로 그대로 저장
            analysis = WeeklyAnalysis.builder()
                    .userId(userId)
                    .weekStartDate(weekStart)
                    .expressionData(
                            WeeklyAnalysis.ExpressionData.builder()
                                    .starScore(newStarScore)
                                    .starCount(1)
                                    .build()
                    )
                    .analyzedAt(LocalDateTime.now())
                    .build();
        } else {
            analysis = analyses.get(0);

            if (analysis.getExpressionData() == null) {
                analysis.setExpressionData(new WeeklyAnalysis.ExpressionData());
            }

            WeeklyAnalysis.ExpressionData expr = analysis.getExpressionData();
            int prevTotalScore = expr.getStarScore() * expr.getStarCount();
            int newCount = expr.getStarCount() + 1;
            int newAverage = Math.round((float)(prevTotalScore + newStarScore) / newCount);

            expr.setStarScore(newAverage);
            expr.setStarCount(newCount);
            analysis.setAnalyzedAt(LocalDateTime.now());
        }

        weeklyAnalysisRepository.save(analysis);
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
