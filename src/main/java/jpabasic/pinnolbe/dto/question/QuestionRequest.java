package jpabasic.pinnolbe.dto.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

@Data
public class QuestionRequest {

    private final String conversation_id;
    private final String user_id;
    private final String question;

    @JsonCreator
    public QuestionRequest(@JsonProperty("conversation_id")String conversation_id, String userId, @JsonProperty("question")String question) {
        this.conversation_id = null;
        this.user_id = userId;
        this.question = question;
    }
}
