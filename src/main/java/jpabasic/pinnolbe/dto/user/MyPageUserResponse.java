package jpabasic.pinnolbe.dto.user;


import io.swagger.v3.oas.annotations.media.Schema;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.payment.Payment;

import java.util.List;

@Schema(description = "MyPage에 사용되는 유저 프로필")
public record MyPageUserResponse (

        @Schema(description = "유저 이름",example="김핀놀")
        String userName,
//        String userProfileUrl,
        @Schema(description = "유저 전화번호",example="010-1234-1234")
        String userPhoneNumber,
        @Schema(description = "부모님 이름",example="김엄마")
        String parentsName,
        @Schema(description = "부모님 전화번호",example="010-1234-1234")
        String parentsPhoneNumber,
        @Schema(description = "결제 내역")
        List<Payment> paymentList,
        @Schema(description = "이용권 관련 내역")
        UserMembershipSummaryResponse membership


){

    public static MyPageUserResponse of(User user,UserMembershipSummaryResponse response){
        return new MyPageUserResponse(
                user.getName(),
                user.getPhoneNumber(),
                user.getParentsName(),
                user.getParentsPhoneNumber(),
                user.getPayments(),
                response

        );
    }


}
