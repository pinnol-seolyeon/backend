package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.question.QAs;
import jpabasic.pinnolbe.dto.question.QuestionSessionDto;
import jpabasic.pinnolbe.dto.question.QuestionRequest;
import jpabasic.pinnolbe.dto.question.QuestionResponse;
import jpabasic.pinnolbe.repository.question.QueCollectionRepository;
import jpabasic.pinnolbe.repository.question.QuestionRepository;
import jpabasic.pinnolbe.service.model.AskQuestionTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QueCollectionRepository queCollectionRepository;
    private final AskQuestionTemplate askQuestionTemplate;

    //사용자별 세션 저장소 //메모리에 저장된 질문 세션 관리 -> 일시적으로 관리
    private final Map<String,QuestionSessionDto> sessionStore=new ConcurrentHashMap<>();


    public QuestionService(QuestionRepository questionRepository, QueCollectionRepository queCollectionRepository,AskQuestionTemplate askQuestionTemplate) {
        this.questionRepository = questionRepository;
        this.queCollectionRepository = queCollectionRepository;
        this.askQuestionTemplate = askQuestionTemplate;
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
        doc.setDate(LocalDate.now());

        queCollectionRepository.save(doc);

        //저장 후 세션 초기화  //sessionStore에서 key가 userId인 entry하나만 삭제
        sessionStore.remove(userId);

    }


    // 질문 내용 요약하기
    public String


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
