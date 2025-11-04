package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jpabasic.pinnolbe.dto.payment.PaymentRequestDto;
import jpabasic.pinnolbe.dto.payment.PaymentResponseDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
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
}
