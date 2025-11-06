package jpabasic.pinnolbe.dto.question;

public record SseSendRequest (
        String eventName,
        Object data
){
}
