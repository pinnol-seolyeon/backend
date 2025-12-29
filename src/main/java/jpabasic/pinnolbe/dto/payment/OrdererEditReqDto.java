package jpabasic.pinnolbe.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrdererEditReqDto (

	@Schema(description="주문자 성함", example="강민서")
	String name,

	@Schema(description = "주문자 전화번호",example="010-1234-1234")
	String phone,

	@Schema(description="주문자 이메일",example="finnol1234@gmail.com")
	String email
){
}
