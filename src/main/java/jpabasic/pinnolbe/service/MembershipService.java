package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.HoldingPeriod;
import jpabasic.pinnolbe.domain.Membership;
import jpabasic.pinnolbe.domain.payment.Payment;
import jpabasic.pinnolbe.dto.user.HoldingRequest;
import jpabasic.pinnolbe.dto.user.MembershipHistoryResponse;
import jpabasic.pinnolbe.dto.user.UserMembershipSummaryResponse;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Member;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatPeriod;

@Service
@Slf4j
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;



    /**
     * 결제 후, membership 생성
     * @param payment
     */
    @Transactional
    public void issueStudyTicket(Payment payment){
        Optional<Membership> lastMembership=membershipRepository.findTopByUserIdOrderByEndDateDesc(payment.getUserId());
        if (lastMembership.isPresent() && !lastMembership.get().isExpired()) {
            // [케이스 A] 연장: 기존 객체 업데이트
            Membership existing = lastMembership.get();
            existing.addPaymentId(payment.getId());
            existing.addQuantity(payment.getQuantity());
            existing.extendDuration(payment.getQuantity());
            membershipRepository.save(existing);
        }
        // [케이스 B] 신규 가입 또는 기존 멤버십이 만료된 경우 -> 신규 생성
        else {
            // 기존 멤버십이 존재한다면 확실히 비활성화(deactivate) 처리
            lastMembership.ifPresent(m -> {
                m.deactivate();
                membershipRepository.save(m);
            });

            // 이전 홀딩 이력 추출 (있으면 가져오고 없으면 빈 리스트)
            List<HoldingPeriod> previousHistory = lastMembership
                    .map(Membership::getHoldingPeriods)
                    .orElse(new ArrayList<>());

            Membership newMembership = Membership.createFromPayment(
                    payment,
                    null,
                    true,
                    previousHistory // 여기서 과거 이력을 넣어줍니다.
            );
            membershipRepository.save(newMembership);
            log.info("[NEW/RE-START] 신규 멤버십 생성: {}", newMembership.getEndDate());
        }

    }

    /**
     * 멤버십 이력 조회
     * @param userId
     * @return
     */
    public List<MembershipHistoryResponse> getAllHistory(String userId) {
        // 해당 유저의 모든 멤버십을 종료일 역순(최신순)으로 조회
        return membershipRepository.findAllByUserIdOrderByEndDateDesc(userId).stream()
                .map(m -> MembershipHistoryResponse.builder()
                        .membershipId(m.getId())
                        .period(m.getStartDate() + " ~ " + m.getEndDate())
                        .status(m.isActive() ? "사용 중" : "만료됨")
                        .holdings(m.getHoldingPeriods()) // 해당 멤버십 내의 홀딩 기록들
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 유저의 정기권내역 조회
     * @param userId
     * @return
     */
    public UserMembershipSummaryResponse getMembershipSummary(String userId) {
        LocalDate today=LocalDate.now();
        //해당 유저의 모든 활성 티켓 조회
        Membership activeMembership = membershipRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(()->new CustomException(ErrorCode.NO_MEMBERSHIP));

        // 2. 전체 시작일(가장 빠른 날)과 종료일(가장 늦은 날) 계산
        LocalDate startDate = activeMembership.getStartDate();
        LocalDate endDate = activeMembership.getEndDate();

        boolean isHolding = false;
        long remainingHoldDays = 0;

        if (activeMembership.getHoldingPeriods() != null) {
            // 2. 현재 날짜가 포함된 홀딩 기간을 찾습니다.
            Optional<HoldingPeriod> currentHold = activeMembership.getHoldingPeriods().stream()
                    .filter(h -> !today.isBefore(h.getHoldStartDate()) && !today.isAfter(h.getHoldEndDate()))
                    .findFirst();

            // 3. 존재한다면 값을 업데이트합니다.
            if (currentHold.isPresent()) {
                isHolding = true;
                remainingHoldDays = ChronoUnit.DAYS.between(today, currentHold.get().getHoldEndDate());
            }
        }

        //총 개수
        long totalMonths = activeMembership.getQuantity();
        return UserMembershipSummaryResponse.builder()
                .membershipName("1개월 권")
                .usagePeriod(formatPeriod(startDate, endDate))
                .totalMonthsText(totalMonths + "개월")
                .isHolding(isHolding)
                .remainingHoldDays(remainingHoldDays)
                .build();
    }

    private String formatPeriod(LocalDate start, LocalDate end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return start.format(formatter) + " ~ " + end.format(formatter);
    }

    /**
     * 홀딩 시작
     */
    @Transactional
    public void holdMembership(String userId,HoldingRequest request){
        Membership membership=membershipRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(()->new CustomException(ErrorCode.NO_MEMBERSHIP));
        membership.addScheduledHolding(request.startDate(),request.endDate());
        membershipRepository.save(membership);
        log.info("[HOLD SCHEDULED] User:{},Period:{}~{},Extended Enddate:{}",
                    userId,request.startDate(),request.endDate(),membership.getEndDate());
    }

    /**
     * 홀딩 해제
     */
    @Transactional
    public void resumeMembership(String userId) {
        // 1. 유효한 멤버십 조회
        Membership membership = membershipRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(()->new CustomException(ErrorCode.NO_MEMBERSHIP));

        // 2. 엔티티의 재개(취소) 로직 호출
        HoldingPeriod newHoldingPeriod=membership.resumeMembership();

        // 3. 변경 내용 저장
        membershipRepository.save(membership);
        log.info("[MEMBERSHIP RESUMED] User: {}, Cancelled Period: {} ~ {}, Restored EndDate: {}",
                userId, newHoldingPeriod.getHoldStartDate(), newHoldingPeriod.getHoldEndDate(), newHoldingPeriod.getDurationDays());
    }





}
