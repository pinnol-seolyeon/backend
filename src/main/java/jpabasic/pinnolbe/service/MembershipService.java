package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.HoldingPeriod;
import jpabasic.pinnolbe.domain.Membership;
import jpabasic.pinnolbe.domain.payment.Payment;
import jpabasic.pinnolbe.dto.user.HoldingRequest;
import jpabasic.pinnolbe.dto.user.UserMembershipSummaryResponse;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
        //유저의 가장 마지막 티켓 만료일 조회
        LocalDate lastEndDate=membershipRepository
                .findTopByUserIdOrderByEndDateDesc(payment.getUserId())
                .map(Membership::getEndDate)
                .orElse(null);

        //도메인 로직에 생성을 위임
        Membership membership=Membership.createFromPayment(payment,lastEndDate);
        membershipRepository.save(membership);

        log.info("[TICKET ISSUED] User: {}, Expiry: {}", payment.getUserId(), membership.getEndDate());
    }

    public UserMembershipSummaryResponse getMembershipSummary(String userId) {
        //해당 유저의 모든 활성 티켓 조회
        List<Membership> activeMemberships = membershipRepository.findAllByUserIdAndActiveTrue(userId);
        if (activeMemberships.isEmpty()) {
            return null;
        }

        // 2. 전체 시작일(가장 빠른 날)과 종료일(가장 늦은 날) 계산
        LocalDate overallStart = activeMemberships.stream()
                .map(Membership::getStartDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate overallEnd = activeMemberships.stream()
                .map(Membership::getEndDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        //총 개수
        int totalQuantity = activeMemberships.size();
        long totalMonths = ChronoUnit.MONTHS.between(overallStart, overallEnd);

        return UserMembershipSummaryResponse.builder()
                .membershipName("1개월 권")
                .totalQuantity(totalQuantity)
                .usagePeriod(formatPeriod(overallStart, overallEnd))
                .totalMonthsText("(" + totalMonths + "개월)")
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
    public void resumeMembership(String userId, HoldingRequest request) {
        // 1. 유효한 멤버십 조회
        Membership membership = membershipRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_MEMBERSHIP));

        // 2. 엔티티의 재개(취소) 로직 호출
        membership.resumeMembership(request.startDate(), request.endDate());

        // 3. 변경 내용 저장
        membershipRepository.save(membership);

        log.info("[MEMBERSHIP RESUMED] User: {}, Cancelled Period: {} ~ {}, Restored EndDate: {}",
                userId, request.startDate(), request.endDate(), membership.getEndDate());
    }





}
