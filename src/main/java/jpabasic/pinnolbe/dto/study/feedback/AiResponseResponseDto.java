package jpabasic.pinnolbe.dto.study.feedback;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiResponseResponseDto {
    private String conversationId;
    private String result;
    // private String[] attachments;
}
