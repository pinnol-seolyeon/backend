package jpabasic.pinnolbe.global.logging.service;

import jpabasic.pinnolbe.global.logging.aop.LogContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SlackService {

    private final RestTemplate restTemplate=new RestTemplate();

    @Value("${slack.webhook.all}") private String allChannelUrl;
    @Value("${slack.webhook.error}") private String errorChannelUrl;

    public void sendFullLog(LogContext ctx, long totalTime, String status) {
        String message = String.format(
                "*Status:* %s\n*User:* %s\n*Request:* %s %s\n*Total Time:* %dms\n*Method Details:*\n%s",
                status, ctx.userId, ctx.httpMethod, ctx.endpoint, totalTime, String.join("\n", ctx.methodDetails)
        );
        send(allChannelUrl, message);
    }

    public void sendErrorLog(LogContext ctx, Exception e, String methodName) {
        String errorMsg = String.format(
                "🚨 *ERROR OCCURRED!*\n*Method:* %s\n*Error:* %s\n*Message:* %s\n*Context:* %s %s",
                methodName, e.getClass().getSimpleName(), e.getMessage(), ctx.httpMethod, ctx.endpoint
        );
        send(errorChannelUrl, errorMsg);
        sendFullLog(ctx, 0, "FAILED"); // 전체 채널에도 실패 로그 전송
    }

    @Async // 비동기로 실행되어 본래 API 응답 속도에 영향을 주지 않음
    public void send(String url, String message) {
        try {
            // 1. 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 2. 바디 구성 (슬랙은 {"text": "내용"} 형식을 기본으로 함)
            Map<String, String> body = new HashMap<>();
            body.put("text", message);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            // 3. 요청 전송
            restTemplate.postForEntity(url, entity, String.class);

        } catch (Exception e) {
            // 슬랙 전송 실패가 서비스 전체의 장애로 이어지지 않도록 예외 처리
            System.err.println("Slack 메시지 전송 실패: " + e.getMessage());
        }
    }
}
