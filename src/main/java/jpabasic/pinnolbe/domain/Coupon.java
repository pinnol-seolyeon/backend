package jpabasic.pinnolbe.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection="coupon")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    @Id
    private String code; //쿠폰 코드(난수)
    private int discountAMount;
    private boolean used;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private String issuedTo; //특정 유저 userId
    private String batchName; //묶음 이름
}
