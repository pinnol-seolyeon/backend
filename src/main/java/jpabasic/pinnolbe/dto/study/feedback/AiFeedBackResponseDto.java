package jpabasic.pinnolbe.dto.study.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiFeedBackResponseDto {
    private String conversationId;
    private String result;
    private String[] attachments;
}
