package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.Payment;
import jpabasic.pinnolbe.dto.payment.PaymentFailResDto;
import jpabasic.pinnolbe.dto.payment.PaymentRequestDto;
import jpabasic.pinnolbe.dto.payment.PaymentResponseDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.PaymentRepository;
import jpabasic.pinnolbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jpabasic.pinnolbe.dto.payment.OrderNameType;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossPaymentService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    @Value("${payments.toss.secret-key}")
    private String tossSecretKey;

    @Value("${payments.toss.success_url}")
    private String successCallBackUrl;

    @Value("${payments.toss.fail_url}")
    private String failCallBackUrl;

    @Transactional
    /**
     * 결제 실패
     */
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

    @Transactional
    /**
     * 결제 승인
     */
    public PaymentResponseDto requestPayments(PaymentRequestDto paymentRequestDto) {
        Long amount=paymentRequestDto.getAmount();
        String payType=paymentRequestDto.getPayType().getName();
        String customerEmail=paymentRequestDto.getCustomerEmail();
        String orderName=paymentRequestDto.getOrderName();

        if(amount==null||amount!=3000){
            throw new CustomException(ErrorCode.PAYMENT_ERROR_ORDER_PRICE);
        }

        if(!payType.equals("CARD") && !payType.equals("카드")){
            throw new CustomException(ErrorCode.PAYMENT_ERROR_ORDER_PAY_TYPE);
        }

        if(!orderName.equals("MONTH") && !orderName.equals("YEAR")){
            throw new CustomException(ErrorCode.PAYMENT_ERROR_ORDER_NAME);
        }

        PaymentResponseDto paymentRes;
        try{
            Payment payment=paymentRequestDto.toEntity();
            userRepository.findByEmail(customerEmail)
                    .ifPresentOrElse(
                            M->M.addPayment(payment)
                            ,()->{
                                throw new CustomException(ErrorCode.USER_NOT_FOUND);
                            }
                    );
            paymentRes =payment.toDto("Y");
            paymentRes.setSuccessUrl(successCallBackUrl);
            paymentRes.setFailUrl(failCallBackUrl);

            paymentRepository.save(payment);
            return paymentRes;
        }catch(Exception e){
            throw new CustomException(ErrorCode.DB_ERROR_SAVE);
        }
    }

    /**
     * 결제 취소 요청
     */
    @Transactional
    public String requestPaymentCancel(String paymentKey,String cancelReason){
        RestTemplate template=new RestTemplate();
        URI uri=URI.create(tossOriginalUrl+paymentKey+"/cancel");

        HttpHeaders headers=new HttpHeaders();
        byte[] secretKeyByte=(testSecretApiKey+":").getBytes(StandardCharsets.UTF_8);
        headers.setBasicAuth(new String(Base64.getEncoder().encode(secretKeyByte)));
        headers.setContentType(MediaType.APPLICATION_JSON);

    }
}
