package jpabasic.pinnolbe.controller.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.TossPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/cancelPayment")
public class CancelPaymentController {

    private final TossPaymentService tossPaymentService;

//    @PostMapping("")
//    @Operation(summary="결제 취소",description = "완료된 결제 건에 대해 결제 취소 요청")
//    public ApiResponse<String> cancelPayment(
//            @Parameter(description = "토스 측 주문 고유 번호",required = true) @RequestParam String paymentKey,
//            @Parameter(description="결제 취소 사유",required = true) @RequestParam String cancelReason
//    ){
//        String result=tossPaymentService.requestPaymentCancel(paymentKey,cancelReason);
//        return ApiResponse.success("결제 취소 요청 완료",result);
//    }
}
