package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.Payment;
import jpabasic.pinnolbe.dto.payment.PaymentRequestDto;
import jpabasic.pinnolbe.dto.payment.PaymentResponseDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.PaymentRepository;
import jpabasic.pinnolbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jpabasic.pinnolbe.dto.payment.OrderNameType;

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
}
