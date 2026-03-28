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
    // 결제 이력 (여러 번 연장할 수 있으므로 List가 적절합니다)
    @Builder.Default
    private List<String> paymentIds = new ArrayList<>();
    private LocalDate startDate; //시작일
    private LocalDate endDate; //종료일
    private Integer quantity;
    private boolean active; //현재 사용 중인지 여부
    private List<HoldingPeriod> holdingPeriods =new ArrayList<>();


    /**
     * 홀딩 해제 및 종료일 단축
     */
    public HoldingPeriod resumeMembership() {
        LocalDate today = LocalDate.now();

        // 1. 오늘 날짜가 포함된 홀딩 기간 찾기
        HoldingPeriod targetPeriod = this.holdingPeriods.stream()
                .filter(h -> !today.isBefore(h.getHoldStartDate()) && !today.isAfter(h.getHoldEndDate()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.HOLD_PERIOD_NOT_FOUND));

        // 2. 남은 홀딩 기간(오늘 포함 ~ 원래 종료일)만큼 멤버십 종료일 앞당기기
        // (예: 원래 3/29~3/31 홀딩이었으면 3일치 회복)
        long remainingDays = ChronoUnit.DAYS.between(today, targetPeriod.getHoldEndDate()) + 1;
        this.endDate = this.endDate.minusDays(remainingDays);

        // 3. 기존 홀딩 기간 삭제
        this.holdingPeriods.remove(targetPeriod);

        // 4. [중요] 오늘 시작한 홀딩이 아니라면(과거에 시작된 경우), 오늘 직전까지의 실제 홀딩 이력만 남김
        if (targetPeriod.getHoldStartDate().isBefore(today)) {
            LocalDate actualEndDate = today.minusDays(1); // 어제부로 종료된 것으로 기록
            long actualHoldDays = ChronoUnit.DAYS.between(targetPeriod.getHoldStartDate(), actualEndDate) + 1;

            HoldingPeriod historyPeriod = new HoldingPeriod(targetPeriod.getHoldStartDate(), actualEndDate, actualHoldDays);
            this.holdingPeriods.add(historyPeriod);
            return historyPeriod;
        }

        // 5. 오늘 시작해서 오늘 취소한 경우: 리스트에 다시 넣지 않고 종료
        return targetPeriod;
    }

    /**
     * 결제 아이디 추가
     */
    public void addPaymentId(String paymentId){
        if(this.paymentIds==null)this.paymentIds=new ArrayList<>();
        this.paymentIds.add(paymentId);
    }

    /**
     * 멤버십 연장
     */
    public void extendDuration(int months){
        this.endDate=this.endDate.plusMonths(months);
    }

    public void addQuantity(Integer quantity){
        this.quantity=this.quantity+quantity;
    }

    /**
     * 결제 정보를 기반으로 새 티켓을 생성하는 팩토리 메서드
     */
    public static Membership createFromPayment(
            Payment payment,
            LocalDate lastEndDate,
            boolean isActive,
            List<HoldingPeriod> periods) {
        //시작일 결정
        LocalDate start = (lastEndDate != null && lastEndDate.isAfter(LocalDate.now()))
                ? lastEndDate : LocalDate.now();

        //기간 계산
        int monthsToAdd = (payment.getQuantity() != null) ? payment.getQuantity() : 1;
        LocalDate end=start.plusMonths(monthsToAdd);

        return Membership.builder()
                .userId(payment.getUserId())
                .paymentIds(new ArrayList<>(List.of(payment.getId())))
                .startDate(start)
                .endDate(end)
                .active(isActive)
                .quantity(payment.getQuantity())
                .holdingPeriods(periods)
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

    public boolean isExpired(){
        return LocalDate.now().isAfter(endDate);
    }

    public void deactivate(){
        this.active=false;
    }

}
