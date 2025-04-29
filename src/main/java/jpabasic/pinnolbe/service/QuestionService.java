package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.question.QAs;
import jpabasic.pinnolbe.dto.question.QuestionRequest;
import jpabasic.pinnolbe.repository.question.QueCollectionRepository;
import jpabasic.pinnolbe.repository.question.QuestionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QueCollectionRepository queCollectionRepository;

    public QuestionService(QuestionRepository questionRepository, QueCollectionRepository queCollectionRepository) {
        this.questionRepository = questionRepository;
        this.queCollectionRepository = queCollectionRepository;
    }


    //질문 내용을 AI 모델에게 전달
    public void askQuestion(QuestionRequest request, User user){


        // 물어본 질문을 해당 repo 에 저장

        // AI에 유저의 질문 전달

        // AI의 답변 내용을 반환
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
