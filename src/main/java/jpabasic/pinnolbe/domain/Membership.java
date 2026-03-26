package jpabasic.pinnolbe.domain;

import jakarta.persistence.Id;
import jpabasic.pinnolbe.domain.payment.Payment;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection="membership")
@RequiredArgsConstructor
@Getter
@Builder
@AllArgsConstructor
public class Membership {

    @Id
    private String id;

    private String userId;
    private LocalDateTime startDate; //시작일
    private LocalDateTime endDate; //종료일

    private boolean active; //현재 사용 가능한지 여부

    // 학습권 만료 여부 확인 로직
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    /**
     * 결제 정보를 기반으로 새 티켓을 생성하는 팩토리 메서드
     */
    public static Membership createFromPayment(Payment payment, LocalDateTime lastEndDate) {
        //시작일 결정
        LocalDateTime start = (lastEndDate != null && lastEndDate.isAfter(LocalDateTime.now()))
                ? lastEndDate : LocalDateTime.now();

        //기간 계산
        int months = (payment.getQuantity() != null) ? payment.getQuantity() : 1;

        return Membership.builder()
                .userId(payment.getUserId())
                .startDate(start)
                .endDate(start.plusMonths(months))
                .build();
    }

}
