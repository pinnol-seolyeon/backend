package jpabasic.pinnolbe.service.question;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jpabasic.pinnolbe.dto.question.QuestionTempCache;
import jpabasic.pinnolbe.dto.question.StreamingResultDto;
import org.springframework.security.core.context.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.Emitter;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseService {

    private final QuestionService questionService;
    @Value("${myapp.fastApi.endpoint}")
    private String fastApiEndpoint;

    private final Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final QuestionTempCache tempCache;

    public SseService(WebClient webClient, ObjectMapper objectMapper, QuestionService questionService, QuestionTempCache tempCache) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.questionService = questionService;
        this.tempCache = tempCache;
    }

    /**
     * 질문 stream 방식으로
     * @param question
     * @param userId
     * @return
     */
    public StreamingResultDto askQuestionStream(String question, String userId) {
        ///SSE sseEmitter 생성 및 등록
        SseEmitter sseEmitter = new SseEmitter(0L); //무제한 타임아웃
        sseEmitterMap.put(userId,sseEmitter);

        StringBuilder accumulatedAnswer=new StringBuilder();
        CompletableFuture<String> resultFuture=new CompletableFuture<>();

//        /// 현재 SecurityContext 보존
//        SecurityContext context= SecurityContextHolder.getContext();

        /// 생명주기 콜백 등록
        sseEmitter.onCompletion(()->sseEmitterMap.remove(userId));
        sseEmitter.onTimeout(()->{
            sseEmitter.complete();
            sseEmitterMap.remove(userId);
            resultFuture.complete(accumulatedAnswer.toString()); //타임아웃 시점에도 반환
        });
        sseEmitter.onError(ex->{
            sseEmitter.completeWithError(ex);
            sseEmitterMap.remove(userId);
            resultFuture.completeExceptionally(ex);
        });

        /// FastAPI에 스트리밍 요청
        webClient.post()
                .uri(fastApiEndpoint+"/chat") //api
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "question", question,
                        "user_id", userId
                ))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve() //HTTP 요청 실행
                .bodyToFlux(String.class) //FastAPI의 SSE 'data' 부분을 JSON 문자열로 받음
                /// Reactor 쓰레드에서도 SecurityContext 유지
//                .publishOn(Schedulers.boundedElastic())
//                .contextWrite(ctx->ctx.put(SecurityContext.class,context))
                .doOnNext(json -> { //event 하나가 들어올 때마다 실행
                    try {
                        //JsonNode로 파싱
                        JsonNode node = objectMapper.readTree(json);

                        // 1. delta 스트림인 경우
                        if (node.has("delta")) {
                            String delta=node.get("delta").asText();
                            if(!delta.isEmpty()){
                                accumulatedAnswer.append(delta); //delta 응답 모아서 누적 저장
                                sseEmitter.send(SseEmitter.event()
                                        .name("message") //프론트에서 "message"로 받음
                                        .data(delta)); //
                            }
                        }

                        // 2. end 이벤트인 경우
                        else if (node.has("event") && "end".equals(node.get("event").asText())) {
                            sseEmitter.send(SseEmitter.event()
                                    .name("end")
                                    .data(json)); //필요하면 전체 json 또는 done같은 간단한 문자열
                        }
                    } catch (IOException e) {
                        log.warn("SSE 전송 중 연결 종료 또는 IO 에러 (userId={}): {}", userId, e.getMessage());
                        sseEmitter.complete();
                        sseEmitterMap.remove(userId);
                        resultFuture.completeExceptionally(e);
                    }
                })
                .doOnError(error -> {
                    //token 만료 (401) 감지
                    if(error instanceof WebClientResponseException wce
                        && wce.getStatusCode()== HttpStatus.UNAUTHORIZED){
                        log.warn("토큰 만료 감지 (userId={})",userId);
                        try{
                            sseEmitter.send(SseEmitter.event()
                                    .name("expired")
                                    .data("access token expired"));
                        }catch(IOException e){
                            log.error("만료 이벤트 전송 실패: {}", e.getMessage());
                        }
                    }else{
                        log.error("FastAPI 스트림 오류 (userId={}): {}", userId, error.getMessage());
                    }
                    sseEmitter.completeWithError(error);
                    resultFuture.completeExceptionally(error);
                    sseEmitterMap.remove(userId);
                })
                .doOnComplete(()->{
                    log.info("FastAPI 스트림 완료 (userId={})", userId);
                    //응답 전체를 String 형태로 반환
                    String finalText= accumulatedAnswer.toString();
                    tempCache.add(userId, question, finalText);
                    resultFuture.complete(finalText);

                    sseEmitter.complete();
                    sseEmitterMap.remove(userId);
                })
                .subscribe();
        return new StreamingResultDto(sseEmitter,resultFuture);

    }

    /*
    실시간 응답 내용을 하나의 result로 반환
     */
    public String getResult(String result){
        StringBuilder accumulatedAnswer=new StringBuilder(result);
        return accumulatedAnswer.toString();
    }

    /*
     * sse를 통한 구독 기능 정의
     */
    public SseEmitter subscribe(String id){
        long timeout=1000L*60*60; //sse emitter 연결 시간 //60초

        //sseEmitter 저장
        SseEmitter sseEmitter=new SseEmitter(timeout);
        sseEmitterMap.put(id,sseEmitter);

        //sseEmitter complete 처리
        sseEmitter.onCompletion(()->{
            log.info("✅ SSE 연결 완료 (id: {})", id);
            sseEmitterMap.remove(id);
        });
        //sseEmitter timeout 발생
        sseEmitter.onTimeout(()->{
            log.warn("⏰ SSE 타임아웃 발생 (id: {})", id);
            sseEmitter.complete();
        });
        //sseEmitter error 발생
        sseEmitter.onError(throwable->{
            log.error("⚠️ SSE 오류 발생 (id: {}, error: {})", id, throwable.getMessage());
            sseEmitter.complete();
        });

        //connect event로 message 발생
        sendToClient(id,"connect","sse connect..");
        return sseEmitter;
    }


    /**
     * sse를 통해 client에 데이터 전달
     * id에 해당되는 sse emitter에 event name의 이벤트로 data 전달
     */
    public void sendToClient(String id,String eventName,Object data){
        SseEmitter sseEmitter=sseEmitterMap.get(id); //저장해둔 sseEmitter 객체 가져옴
        try{
            //클라이언트로 데이터를 즉시 푸시(전송)
            sseEmitter.send(
                    //sseEmitter.event() 체이닝: sse 이벤트의 메타데이터 구성
                    sseEmitter.event()
                            .id(id)
                            .name(eventName)
                            .data(data)
            );
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}
