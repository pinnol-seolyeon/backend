package jpabasic.pinnolbe.controller;

import jpabasic.pinnolbe.service.question.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/SSE")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

//    @GetMapping("/subscribe/{id}")
//    public SseEmitter subscribe(@PathVariable String id) {
//        return sseService.subscribe(id);
//    }
//
//    @PostMapping("/send/{id}")
//    public void sendAlarm(@PathVariable String id, @RequestBody SseSendRequest request) {
//        sseService.sendToClient(id,request.eventName(),request.data());
//    }


}
