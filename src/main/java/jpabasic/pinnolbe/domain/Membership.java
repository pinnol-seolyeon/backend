package jpabasic.pinnolbe.domain;

import jakarta.persistence.Id;
import jpabasic.pinnolbe.domain.payment.Payment;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import lombok.*;
import org.springframework.cglib.core.Local;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
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
    private LocalDate startDate; //시작일
    private LocalDate endDate; //종료일
    private boolean active; //현재 사용 중인지 여부
    private boolean isHolding; //현재 홀딩 상태 여부
    private List<HoldingPeriod> holdingPeriods =new ArrayList<>();

    /**
     * 홀딩 시작
     */
    public void startHolding() {
        this.isHolding = true;
        this.holdingPeriods.add(new HoldingPeriod(LocalDate.now()));
    }

    /**
     * 홀딩 해제 및 종료일 연장
     */
    public void resumeMembership(LocalDate start, LocalDate end) {
        HoldingPeriod targetPeriod=this.holdingPeriods.stream()
                .filter(h->h.getHoldStartDate().equals(start) && h.getHoldEndDate().equals(end))
                .findFirst()
                .orElseThrow(()->new CustomException(ErrorCode.HOLD_PERIOD_NOT_FOUND));
        this.endDate=this.endDate.minusDays(targetPeriod.getDurationDays());
        this.holdingPeriods.remove(targetPeriod);
    }

    /**
     * 결제 정보를 기반으로 새 티켓을 생성하는 팩토리 메서드
     */
    public static Membership createFromPayment(Payment payment, LocalDate lastEndDate) {
        //시작일 결정
        LocalDate start = (lastEndDate != null && lastEndDate.isAfter(LocalDate.now()))
                ? lastEndDate : LocalDate.now();

        //기간 계산
        int months = (payment.getQuantity() != null) ? payment.getQuantity() : 1;

        return Membership.builder()
                .userId(payment.getUserId())
                .startDate(start)
                .endDate(start.plusMonths(months))
                .build();
    }

    public void addScheduledHolding(LocalDate start, LocalDate end) {
        // 1. 검증 로직 (엔티티 내부에서 수행)
        if (start.isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.CANNOT_HOLD_PAST_DATE);
        }
        if (start.isAfter(this.endDate)) {
            throw new CustomException(ErrorCode.INVALID_HOLD_START_DATE);
        }

        boolean isOverlapped = this.holdingPeriods.stream()
                .anyMatch(h -> !start.isAfter(h.getHoldEndDate()) && !end.isBefore(h.getHoldStartDate()));
        if (isOverlapped) {
            throw new CustomException(ErrorCode.OVERLAPPED_HOLD_PERIOD);
        }

        // 2. 기간 계산 및 상태 변경
        long diffDays = ChronoUnit.DAYS.between(start, end) + 1;
        this.holdingPeriods.add(new HoldingPeriod(start, end, diffDays));

        // 3. [중요] 필드에 직접 할당해야 저장됩니다!
        this.endDate = this.endDate.plusDays(diffDays);
    }

}
