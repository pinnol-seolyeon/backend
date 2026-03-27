package jpabasic.pinnolbe.domain;

import jakarta.persistence.Id;
import jpabasic.pinnolbe.domain.payment.Payment;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Document(collection="membership")
@RequiredArgsConstructor
@Getter
@Builder
@AllArgsConstructor
public class Membership extends BaseEntity{

    @Id
    private String id;

    private String userId;
    private LocalDateTime startDate; //시작일
    private LocalDateTime endDate; //종료일

    private boolean active; //현재 사용 가능한지 여부

    private boolean isHolding; //현재 홀딩 상태 여부
    private List<HoldingHistory> holdingHistories=new ArrayList<>();

    // 학습권 만료 여부 확인 로직
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    /**
     * 홀딩 시작
     */
    public void startHolding() {
//        if (this.isHolding) throw new CustomException(ErrorCode.ALREADY_HOLDING);
        this.isHolding = true;
        this.holdingHistories.add(new HoldingHistory(LocalDateTime.now()));
    }

    /**
     * 홀딩 해제 및 종료일 연장
     */
    public void resumeMembership() {
        if (!this.isHolding) return;

        // 가장 최근 홀딩 기록을 찾아 기간 계산
        HoldingHistory lastHistory = holdingHistories.get(holdingHistories.size() - 1);
        lastHistory.close(LocalDateTime.now());

        // 홀딩한 기간(일수)만큼 endDate 연장
        long holdingDays = ChronoUnit.DAYS.between(lastHistory.getStartDate(), lastHistory.getEndDate());
        this.endDate = this.endDate.plusDays(holdingDays);

        this.isHolding = false;
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
