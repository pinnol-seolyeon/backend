package jpabasic.pinnolbe.controller;


import jpabasic.pinnolbe.service.TtsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/tts")
public class TtsController {

    private final TtsService ttsService;

    @Autowired
    public TtsController(TtsService ttsService) {
        this.ttsService = ttsService;
    }

    /**
     * POST /api/tts/text
     * Request Body(JSON): { "text": "읽어줄 문장" }
     * Response: audio/mpeg (MP3 바이트)
     */
    @PostMapping("/text")
    public ResponseEntity<byte[]> synthesizeByText(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        byte[] audioBytes;
        try {
            // TtsService에서 byte[] 리턴받기
            audioBytes = ttsService.synthesizeToByteArray(text);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

        // 응답 헤더에 Content-Type: audio/mpeg 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        // (선택) 파일 이름을 지정하고 싶다면 아래처럼 추가할 수도 있습니다.
        // headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"tts.mp3\"");

        // MP3 byte를 그대로 응답 바디에 담아 리턴
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(audioBytes);
    }
}
