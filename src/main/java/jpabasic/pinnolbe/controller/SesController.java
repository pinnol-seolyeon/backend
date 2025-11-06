package jpabasic.pinnolbe.controller;

import jpabasic.pinnolbe.dto.question.SseSendRequest;
import jpabasic.pinnolbe.service.question.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/SSE")
@RequiredArgsConstructor
public class SesController {

    private final SseService sseService;

    @GetMapping("/subscribe/{id}")
    public SseEmitter subscribe(@PathVariable String id) {
        return sseService.subscribe(id);
    }

    @PostMapping("/send/{id}")
    public void sendAlarm(@PathVariable String id, @RequestBody SseSendRequest request) {
        sseService.sendToClient(id,request.eventName(),request.data());
    }


}
