package jpabasic.pinnolbe.service.model;

import jpabasic.pinnolbe.dto.question.QuestionRequest;
import jpabasic.pinnolbe.dto.question.QuestionResponse;
import jpabasic.pinnolbe.dto.question.QuestionSummaryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
/// Rest Template을 통해서 fastAPI와 통신
public class AskQuestionTemplate {

    @Value("${myapp.fastApi.endpoint}")
    private String fastApiEndpoint; ///http://127.0.0.1:8000/api/rag/chat

    public QuestionResponse askQuestionToAI(QuestionRequest questionRequest) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<QuestionRequest> request = new HttpEntity<>(questionRequest, headers);

        ResponseEntity<QuestionResponse> response = restTemplate.exchange(
                fastApiEndpoint+"/chat", HttpMethod.POST, request, QuestionResponse.class
        );
        System.out.println("🧪 FastAPI Raw Response: " + response.getBody());

        QuestionResponse body=response.getBody();
        System.out.println("🧪 응답 객체 = " + body);
        System.out.println("✅ result 값 = " + (body != null ? body.getResult() : "null"));
        return body;
    }


     /// /api/rag/question-summary

     //질문 요약+질문 개수
    public QuestionSummaryDto summaryQuestionsByAI(List<String> questions){
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<Map<String,String>> qaList=new ArrayList<>();
        for (int i=0;i<questions.size();i+=2){
            String question=questions.get(i);
            String answer=(i+1<questions.size())?questions.get(i+1):"";
            qaList.add(Map.of("question",question,"answer",answer));
        }

        Map<String, Object> payload = Map.of("qa_list", qaList);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<QuestionSummaryDto> response = restTemplate.exchange(
                fastApiEndpoint+"/question-summary", HttpMethod.POST, request, QuestionSummaryDto.class
        );
        System.out.println("🧪 FastAPI Raw Response: " + response.getBody());

        String body=response.getBody().getSummary();

        System.out.println("🐛 응답 객체="+body);
        return new QuestionSummaryDto(body,qaList.size());
    }

}
