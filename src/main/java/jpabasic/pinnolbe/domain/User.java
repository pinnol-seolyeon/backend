package jpabasic.pinnolbe.domain;

import com.mongodb.lang.Nullable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
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

    //가입자(아이)의 이름-카카오톡
    private String name;

    //핀놀 닉네임
    private String nickname;

    //유저 전화번호
    @Column(name="phone_number")
    private String phoneNumber;

    @Nullable
    private String studySessionLogId; //최근 학습 상태(진도)

    //여태까지 모은 코인 개수
    private int reward=0;

    //부모 전화번호
    @Nullable
    @Column(name = "parents_phone_number")
    private String parentsPhoneNumber;

    //부모 이름
    @Nullable
    @Column(name="parents_name")
    private String parentsName;

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
