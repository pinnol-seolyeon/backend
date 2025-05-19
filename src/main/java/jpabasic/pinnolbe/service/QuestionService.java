package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.question.QAs;
import jpabasic.pinnolbe.dto.question.QuestionRequest;
import jpabasic.pinnolbe.dto.question.QuestionResponse;
import jpabasic.pinnolbe.repository.question.QueCollectionRepository;
import jpabasic.pinnolbe.repository.question.QuestionRepository;
import jpabasic.pinnolbe.service.model.AskQuestionTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Date;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QueCollectionRepository queCollectionRepository;
    private final AskQuestionTemplate askQuestionTemplate;


    public QuestionService(QuestionRepository questionRepository, QueCollectionRepository queCollectionRepository,AskQuestionTemplate askQuestionTemplate) {
        this.questionRepository = questionRepository;
        this.queCollectionRepository = queCollectionRepository;
        this.askQuestionTemplate = askQuestionTemplate;
    }


    //질문 내용을 AI 모델에게 전달
    public QuestionResponse askQuestion(QuestionRequest request, User user){

//        // 물어본 질문
//        String question=request.getQuestion();

        // AI에 유저의 질문 전달
        try {
            QuestionResponse answer = askQuestionTemplate.askQuestionToAI(request);

            // AI의 답변 내용을 반환
            return answer;
        }catch(RestClientException e){
            throw new RuntimeException("AI 서버 호출 중 오류 발생", e);
        }

    }



    //모든 질문+답변 한꺼번에 DB에 저장하기
    public ResponseEntity<QueCollection> saveAllQAs(User user, QAs queAnsList){
        int part=queAnsList.getPart();

        QueCollection queCollection = queCollectionRepository.findByPart(part);
        if(queCollection==null){
            queCollection=makeQueCollectionRepo(part,user);
        }

        queCollection.setDate(new Date());
        queCollection.setQuestions(queAnsList.getQuestionAnswers());
        queCollectionRepository.save(queCollection);

        return ResponseEntity.ok(queCollection);

    }



    //단원별로 질문 모아두는 레포 생성
    public QueCollection makeQueCollectionRepo(int part, User user){

        QueCollection queCollection=new QueCollection(part,user);
        queCollection.setDate(new Date());

        queCollectionRepository.save(queCollection);
        return queCollection;

    }
}
