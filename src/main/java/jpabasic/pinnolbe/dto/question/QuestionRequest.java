package jpabasic.pinnolbe.dto.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

@Data
public class QuestionRequest {

    private String user_id;
    private int level;
    private int order;
    private String question;

    @JsonCreator
    public QuestionRequest(
            @JsonProperty("user_id") String userId,
            @JsonProperty("question")String question,
            int level,
            int order) {
        this.user_id = userId;
        this.question = question;
        this.level = level;
        this.order = order;
    }
}
