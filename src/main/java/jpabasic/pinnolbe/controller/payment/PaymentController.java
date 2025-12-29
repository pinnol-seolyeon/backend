package jpabasic.pinnolbe.controller.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.payment.OrdererEditReqDto;
import jpabasic.pinnolbe.dto.payment.OrdererResDto;
import jpabasic.pinnolbe.dto.payment.PaymentFailResDto;
import jpabasic.pinnolbe.dto.payment.PaymentRequestDto;
import jpabasic.pinnolbe.dto.payment.PaymentResHandleCardDto;
import jpabasic.pinnolbe.dto.payment.PaymentResponseDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.TossPaymentService;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

	private final TossPaymentService tossPaymentService;
	private final UserService userService;

	@PostMapping
	@Operation(summary = "결제 요청", description = "결제 요청에 필요한 값들을 반환")
	public ApiResponse<PaymentResponseDto> requestPayments(
		@Parameter(description = "요청 객체", required = true) @RequestBody PaymentRequestDto paymentReq
	) {
		PaymentResponseDto result = tossPaymentService.requestPayments(paymentReq);
		return ApiResponse.success("결제 승인 완료", result);
	}

	@GetMapping("/fail")
	@Operation(summary = "결제 실패 리다이렉트", description = "결제 실패 시 에러코드 및 에러메시지를 반환합니다.")
	public ApiResponse<PaymentFailResDto> failPayment(
		@Parameter(description = "에러 코드", required = true) @RequestParam(name = "code") String errorCode,
		@Parameter(description = "에러 메시지", required = true) @RequestParam(name = "message") String errorMsg,
		@Parameter(description = "우리측 주문 고유 번호", required = true) @RequestParam(name = "orderId") String orderId

	) {
		PaymentFailResDto result = tossPaymentService.requestFail(errorCode, errorMsg, orderId);
		return ApiResponse.success("결제 실패", result);
	}

	@GetMapping("/success")
	@Operation(summary = "결제 성공 리다이렉트", description = "결제 성공 시 최종 결제 승인 요청을 보냄")
	public ApiResponse<PaymentResHandleCardDto> successPayment(
		@Parameter(description = "우리가 정한 주문 고유번호", required = true, example = "a123456") @RequestParam(name = "orderId") String orderId,
		@Parameter(description = "토스페이먼츠에서 정한 결제 구분용 키 ", required = true) @RequestParam(name = "paymentKey") String paymentKey,
		@Parameter(description = "실제 결제 금액", required = true) @RequestParam(name = "amount") Long amount
	) {
		log.info("[SUCCESS URL HIT] orderId={}, paymentKey={}", orderId, paymentKey);
		PaymentResHandleCardDto result = tossPaymentService.requestFinalPayment(paymentKey, orderId, amount);
		return ApiResponse.success("결제 성공", result);
	}

	@GetMapping("/orderer-info")
	@Operation(summary = "주문자 정보")
	public ApiResponse<OrdererResDto> ordererInfo() {
		User user = userService.getUserInfo();
        OrdererResDto result=tossPaymentService.orderInfo(user);
        return ApiResponse.success("주문자 정보입니다.",result);
	}

    @PatchMapping("/edit/orderer-info")
    @Operation(summary="주문자 정보 수정")
    public ApiResponse<OrdererResDto> editOrdererInfo(
        @Parameter(description="주문자 id") @RequestParam(name="ordererId") String ordererId,
        @RequestBody OrdererEditReqDto request
    ){
        OrdererResDto result=tossPaymentService.editOrderInfo(ordererId,request);
        return ApiResponse.success("수정 완료되었습니다.",result);
    }


}
