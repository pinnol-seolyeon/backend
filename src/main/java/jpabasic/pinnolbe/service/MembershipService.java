package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.Membership;
import jpabasic.pinnolbe.domain.payment.Payment;
import jpabasic.pinnolbe.dto.user.UserMembershipSummaryResponse;
import jpabasic.pinnolbe.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        LocalDateTime lastEndDate=membershipRepository
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
        LocalDateTime overallStart = activeMemberships.stream()
                .map(Membership::getStartDate)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        LocalDateTime overallEnd = activeMemberships.stream()
                .map(Membership::getEndDate)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        //총 개수
        int totalQuantity = activeMemberships.size();
        long totalMonths = ChronoUnit.MONTHS.between(overallStart, overallEnd);

        return UserMembershipSummaryResponse.builder()
                .ticketName("1개월 권")
                .totalQuantity(totalQuantity)
                .usagePeriod(formatPeriod(overallStart, overallEnd))
                .totalMonthsText("(" + totalMonths + "개월)")
                .build();
    }

    private String formatPeriod(LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return start.format(formatter) + " ~ " + end.format(formatter);
    }



}
