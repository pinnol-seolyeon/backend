package jpabasic.pinnolbe.domain;

import com.mongodb.lang.Nullable;
import io.swagger.v3.oas.annotations.media.Schema;
import jpabasic.pinnolbe.domain.payment.Payment;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection="user")
@RequiredArgsConstructor
@Setter
@Getter
@Builder
@AllArgsConstructor
public class User {
    @Id
    private String id;
    //암호화된 name
    private String username;

    private String email;

    private String role;

    //가입자(아이)의 이름
    private String name;

    //현재 학습중인 교재 저장
    @Nullable
    private String studyId;

    @Nullable
    private String studySessionLogId; //최근 학습 상태(진도)

    //여태까지 모은 코인 개수
    private int reward=0;

    //부모 전화번호
    @Nullable
    private String phoneNumber;

    //개인정보 수집 이용 동의여부
    @Nullable
    private Boolean agreement;

    @Schema(description="결제 내역")
    private List<Payment> payments=new ArrayList<>();

    public void addPayment(Payment payment){
        if(payments==null){
            payments=new ArrayList<>();
        }
        payments.add(payment);
    }



}
