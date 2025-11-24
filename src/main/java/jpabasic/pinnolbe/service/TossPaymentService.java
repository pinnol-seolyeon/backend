package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.payment.Payment;
import jpabasic.pinnolbe.dto.payment.PaymentFailResDto;
import jpabasic.pinnolbe.dto.payment.PaymentRequestDto;
import jpabasic.pinnolbe.dto.payment.PaymentResponseDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.payment.PaymentRepository;
import jpabasic.pinnolbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class TossPaymentService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    @Value("${payments.toss.secret-key}")
    private String testSecretApiKey;

    @Value("${payments.toss.success-url}")
    private String successCallBackUrl;

    @Value("${payments.toss.fail-url}")
    private String failCallBackUrl;

//    @Value("${payments.toss.cancel_url}")
//    private String tossOriginalUrl;


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
                                throw new CustomException(ErrorCode.PAYMENT_USER_EMAIL_NOT_FOUND);
                            }
                    );
            paymentRes =payment.toDto("Y");

            System.out.println("successCallBackUrl = " + successCallBackUrl);
            System.out.println("failCallBackUrl = " + failCallBackUrl);

            paymentRes.setSuccessUrl(successCallBackUrl);
            paymentRes.setFailUrl(failCallBackUrl);

            paymentRepository.save(payment);
            return paymentRes;
        }catch(Exception e){
            e.printStackTrace();
            throw new CustomException(ErrorCode.DB_ERROR_SAVE);
        }
    }

    /**
     * 결제 취소 요청
     */
//    @Transactional
//    public boolean requestPaymentCancel(String paymentKey,String cancelReason){
//        RestTemplate template=new RestTemplate();
//        URI uri=URI.create(tossOriginalUrl+paymentKey+"/cancel");
//
//        HttpHeaders headers=new HttpHeaders();
//        byte[] secretKeyByte=(testSecretApiKey+":").getBytes(StandardCharsets.UTF_8);
//        headers.setBasicAuth(new String(Base64.getEncoder().encode(secretKeyByte)));
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//        JSONObject param=new JSONObject();
//        param.put("cancelReason",cancelReason);
//
//        PaymentCancelDto paymentCancelDto;
//        try{
//            paymentCancelDto=template.postForObject(
//                    uri,
//                    new HttpEntity<>(param,headers),
//                    PaymentCancelDto.class
//            );
//        }catch(Exception e){
//            throw new CustomException(ErrorCode.PAYMENT_CANCEL_ERROR);
//        }
//
//        if(paymentCancelDto==null) return false;
//
//        Long cancelAmount=paymentCancelDto.getCancels()[0].getCancelAmount();
//        try{
//            paymentRepository
//                    .findByPaymentKey(paymentKey)
//                    .filter(P->P.getAmount().equals(cancelAmount))
//                    .orElseThrow(()->new CustomException(ErrorCode.PAYMENT_ERROR_ORDER_NOTFOUND))
//                    .getCustomer()
//                    .addCancelPayment(paymentCancelDto.toCancelPayment());
//            return true;
//        }catch(Exception e){
//            throw new CustomException(ErrorCode.DB_ERROR_SAVE);
//        }
//
//    }
}
