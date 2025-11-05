package jpabasic.pinnolbe.service.question;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseService {

    private final Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

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
