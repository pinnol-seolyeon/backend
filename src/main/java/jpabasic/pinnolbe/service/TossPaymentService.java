package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.payment.OrdererProfile;
import jpabasic.pinnolbe.domain.payment.Payment;
import jpabasic.pinnolbe.dto.payment.OrdererEditReqDto;
import jpabasic.pinnolbe.dto.payment.OrdererResDto;
import jpabasic.pinnolbe.dto.payment.PaymentFailResDto;
import jpabasic.pinnolbe.dto.payment.PaymentRequestDto;
import jpabasic.pinnolbe.dto.payment.PaymentResHandleCardDto;
import jpabasic.pinnolbe.dto.payment.PaymentResponseDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.payment.OrdererProfileRepository;
import jpabasic.pinnolbe.repository.payment.PaymentRepository;
import jpabasic.pinnolbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossPaymentService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final OrdererProfileRepository ordererProfileRepository;
    @Value("${payments.toss.secret-key}")
    private String testSecretApiKey;

    @Value("${payments.toss.original-url}")
    private String tossOriginalUrl;


    /*
     * 결제 실패
     */
    @Transactional
    public PaymentFailResDto requestFail(String errorCode,String errorMsg,String orderId){
        Payment payment=paymentRepository.findByOrderId(orderId)
                .orElseThrow(()-> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        payment.setPaySuccessYn("N");
        payment.setPayFailReason(errorMsg);
        paymentRepository.save(payment);

        return PaymentFailResDto
                .builder()
                .orderId(orderId)
                .errorCode(errorCode)
                .errorMsg(errorMsg)
                .build();
    }


    /*
     * 결제 승인: 유저 이메일 검증
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto requestPayments(PaymentRequestDto paymentRequestDto) {

        String customerEmail=paymentRequestDto.getCustomerEmail();

        PaymentResponseDto paymentRes;
        try{
            Payment payment=paymentRequestDto.toEntity();
            userRepository.findByEmail(customerEmail)
                    .ifPresentOrElse(
                            M->M.addPayment(payment)
                            ,()->{
                                throw new CustomException(ErrorCode.PAYMENT_USER_EMAIL_NOT_FOUND);
                            }
                    );
            paymentRes =payment.toDto();

            paymentRepository.save(payment);
            return paymentRes;
        }catch(Exception e){
            e.printStackTrace();
            throw new CustomException(ErrorCode.DB_ERROR_SAVE);
        }
    }

    /*
     * 검증 전용
     */
	public Payment verifyRequest(String orderId, Long amount){
            Payment payment=paymentRepository.findByOrderId(orderId)
                .orElseThrow(()->new CustomException(ErrorCode.PAYMENT_ERROR));

            //이미 결제 완료된 주문이면 그대로 반환(멱등성)
            if("Y".equals(payment.getPaySuccessYn())){
                return payment;
            }

            //금액 검증
            if(!payment.getAmount().equals(amount)){
                throw new CustomException(ErrorCode.PAYMENT_ERROR_ORDER_AMOUNT);
            }

            return payment;
    }

    /*
     * Toss Confirm + 상태 변경
     */
    @Transactional
    public PaymentResHandleCardDto requestFinalPayment(String paymentKey,String orderId,Long amount){

        Payment payment=verifyRequest(orderId,amount);

        //confirm 요청
        PaymentResHandleCardDto result=tossConfirm(paymentKey,orderId,amount);
        log.info("[CONFIRM RESPONSE] {}", result);

        if(!"DONE".equals(result.getStatus())){
            throw new CustomException(ErrorCode.PAYMENT_ERROR);
        }

        //confirm 성공 시 DB 업데이트
        payment.setPaymentKey(result.getPaymentKey());
        // payment.setPaySuccessYn("Y");
        payment.setApprovedAt(result.getApprovedAt().toString());
        payment.setMethod(result.getMethod()); //결제 방식
        payment.setStatus(result.getStatus()); //결제 성공 여부
        paymentRepository.save(payment);

        return result;
    }

    /*
     * 토스 결제 confirm
     */
    private PaymentResHandleCardDto tossConfirm(String paymentKey,String orderId,Long amount){
        RestTemplate rest=new RestTemplate();
        HttpHeaders headers=new HttpHeaders();
        testSecretApiKey=testSecretApiKey+":";
        String encoded = Base64.getEncoder()
            .encodeToString((testSecretApiKey + ":").getBytes(StandardCharsets.UTF_8));

        headers.set("Authorization", "Basic " + encoded);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String,Object> body=new HashMap<>();
        body.put("paymentKey",paymentKey);
        body.put("orderId",orderId);
        body.put("amount",amount);

        HttpEntity<Map<String,Object>> request=
            new HttpEntity<>(body,headers);

        ResponseEntity<PaymentResHandleCardDto> response=
            rest.postForEntity(
                tossOriginalUrl,
                request,
                PaymentResHandleCardDto.class
            );
        PaymentResHandleCardDto result=response.getBody();
        return result;
    }

    /*
     * 주문자 정보 불러오기
     */
    @Transactional
    public OrdererResDto orderInfo(User user) {

        OrdererProfile profile = ordererProfileRepository.findByUserId(user.getId())
            .orElseGet(() -> ordererProfileRepository.save(OrdererProfile.create(user)));

        return OrdererResDto.toDto(profile);
    }


    /*
     * 주문자 정보 수정 후 저장
     */
    @Transactional
    public OrdererResDto editOrderInfo(String ordererId, OrdererEditReqDto request){
        OrdererProfile profile=ordererProfileRepository.findById(ordererId)
            .orElseThrow(()->new CustomException(ErrorCode.ORDERER_NOT_FOUND));

        profile.update(request);
        ordererProfileRepository.save(profile);

        return OrdererResDto.toDto(profile);
    }


}
