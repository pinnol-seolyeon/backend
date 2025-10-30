package jpabasic.pinnolbe.dto.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

@Data
public class QuestionRequest {

    private final String conversation_id;
    private final String question;

    @JsonCreator
    public QuestionRequest(@JsonProperty("conversation_id")String conversation_id, @JsonProperty("question")String question) {
        this.conversation_id = conversation_id;
        this.question = question;
    }
}
