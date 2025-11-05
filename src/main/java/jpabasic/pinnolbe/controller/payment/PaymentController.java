package jpabasic.pinnolbe.controller.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jpabasic.pinnolbe.dto.payment.PaymentFailResDto;
import jpabasic.pinnolbe.dto.payment.PaymentRequestDto;
import jpabasic.pinnolbe.dto.payment.PaymentResponseDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.TossPaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final TossPaymentService tossPaymentService;

    public PaymentController(TossPaymentService tossPaymentService) {
        this.tossPaymentService = tossPaymentService;
    }

    @PostMapping
    @Operation(summary="결제 요청",description = "결제 요청에 필요한 값들을 반환")
    public ApiResponse<PaymentResponseDto> requestPayments(
            @Parameter(description = "요청 객체",required = true) @RequestBody PaymentRequestDto paymentReq
    ){
            PaymentResponseDto result=tossPaymentService.requestPayments(paymentReq);
            return ApiResponse.success("결제 승인 완료",result);
    }

    @GetMapping("/fail")
    @Operation(summary="결제 실패 리다이렉트",description="결제 실패 시 에러코드 및 에러메시지를 반환합니다.")
    public ApiResponse<PaymentFailResDto> failPayment(
            @Parameter(description = "에러 코드",required = true) @RequestParam(name="code") String errorCode,
            @Parameter(description = "에러 메시지",required = true) @RequestParam(name="message") String errorMsg,
            @Parameter(description = "우리측 주문 고유 번호",required = true) @RequestParam(name="orderId") String orderId
            
    ){
        PaymentFailResDto result=tossPaymentService.requestFail(errorCode,errorMsg,orderId);
        return ApiResponse.success("결제 승인 완료",result);
    }

    @GetMapping("/success")
    @Operation(summary="결제 성공 리다이렉트",description="결제 성공 시 paymentKey 등 반환")
    public ApiResponse<PaymentResponseDto> successPayment(
            PaymentResponseDto request
            )
            tossPaymentService.successPayment(request)



}
