package jpabasic.pinnolbe.dto.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

@Data
public class QuestionRequest {

    private final String question;

    @JsonCreator
    public QuestionRequest(@JsonProperty("question")String question) {
        this.question = question;
    }

}
