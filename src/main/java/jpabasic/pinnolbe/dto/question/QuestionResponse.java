package jpabasic.pinnolbe.dto.question;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) //FastAPI가 응답에 다른 필드를 섞어서 줄 경우, 이를 무시하지 않으면 Jackson 매핑이 실패하고 nulll 발생
public class QuestionResponse {

    @JsonProperty("result")
    private String result;


}
