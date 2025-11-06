package jpabasic.pinnolbe.dto.question;


import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

public class StreamingResultDto {
    private final SseEmitter emitter;
    private final CompletableFuture<String> result;

    public StreamingResultDto(
            SseEmitter emitter, CompletableFuture<String> result
    ) {
        this.emitter = emitter;
        this.result = result;
    }

    public SseEmitter getEmitter() {return emitter;}
    public CompletableFuture<String> getResult() {return result;}
}
