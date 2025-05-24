package jpabasic.pinnolbe.service.model;

import jpabasic.pinnolbe.dto.question.QuestionRequest;
import jpabasic.pinnolbe.dto.question.QuestionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
                fastApiEndpoint, HttpMethod.POST, request, QuestionResponse.class
        );
        System.out.println("🧪 FastAPI Raw Response: " + response.getBody());

        QuestionResponse body=response.getBody();
        System.out.println("🧪 응답 객체 = " + body);
        System.out.println("✅ result 값 = " + (body != null ? body.getResult() : "null"));
        return body;
    }

}
