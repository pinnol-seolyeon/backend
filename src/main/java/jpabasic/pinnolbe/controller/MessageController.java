package jpabasic.pinnolbe.controller;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.model.MessageStatusType;
import net.nurigo.sdk.message.request.MessageListRequest;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.MessageListResponse;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import net.nurigo.sdk.message.service.MessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/message")
@Slf4j
public class MessageController {

//    @Value("${sms.api.key}")
//    private String apiKey;
//
//    @Value("${sms.api.secret}")
//    private String apiSecret;
//
//    private DefaultMessageService messageService;
//
//    @PostConstruct
//    public void init() {
//        log.info("초기화 시작-apiKey:{}", apiKey);
//        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
//        log.info("✅MessageService 초기화 완료");
//
//
//    }
//
//
//    //장문 메시지(LMS) 보내기
//    @PostMapping("/send-LMS")
//    public SingleMessageSentResponse sendLMS() {
//        Message message = new Message();
//
//        //010xxxxxxxx형태
//        message.setFrom("01020090882");
//        message.setTo("01083780260");
//        message.setText("~"); //글자 수 늘어나면 자동으로 SMS -> LMS 전환
//
////        try{
//            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
//            //발송에 실패한 메시지 목록 확인
////        }catch(NurigoMessageNotReceivedException e){
////            System.out.println(e.getFailedMessageList());
////            System.out.println(e.getMessage());
////        }catch(Exception e){
////            System.out.println(e.getMessage());
////        }
//
//        return response;
//    }
//
//    //메시지 조회 //유저가 자신이 받은 문자들 리스트 조회 가능하도록
//    @GetMapping("/get-list")
//    public MessageListResponse getMessageList() {
//        MessageListRequest request = new MessageListRequest();
//
//        //검색할 건 수 최대치 지정
//        request.setLimit(20);
//        request.setTo("검색할 수신번호");
//        request.setStatus(MessageStatusType.COMPLETE); //전송 완료된 메시지 검색
//
//        //검색할 메시지 목록
//        ArrayList<String> messageIds=new ArrayList<>();
//        messageIds.add("검색할 메시지 ID");
//        request.setMessageIds(messageIds);
//
//        MessageListResponse response = this.messageService.getMessageList(request);
//        System.out.println(response);
//
//        return response;
//
//
//    }

}
