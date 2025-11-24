package jpabasic.pinnolbe.service.model;

import jpabasic.pinnolbe.dto.review.ReviewReqDto;
import jpabasic.pinnolbe.dto.review.ReviewQuizResDto;
import jpabasic.pinnolbe.dto.review.TextReviewResDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ReviewAITemplate {

    @Value("${myapp.fastApi.endpoint}")
    private String fastApiEndpoint;

    public ReviewQuizResDto makeReviewQuizByAI(ReviewReqDto request){
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ReviewReqDto> entity=new HttpEntity<>(request, headers);

        ResponseEntity<ReviewQuizResDto> response = restTemplate.exchange(
                fastApiEndpoint+"/quiz-review",
                HttpMethod.POST,
                entity,
                ReviewQuizResDto.class
        );

        return response.getBody();
    }

    public TextReviewResDto makeTextReviewByAI(ReviewReqDto request){
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ReviewReqDto> entity=new HttpEntity<>(request, headers);
        ResponseEntity<TextReviewResDto> response = restTemplate.exchange(
                fastApiEndpoint+"/text-review",
                HttpMethod.POST,
                entity,
                TextReviewResDto.class
        );
        return response.getBody();
    }
}
