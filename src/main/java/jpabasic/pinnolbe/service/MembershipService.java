package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.Membership;
import jpabasic.pinnolbe.domain.payment.Payment;
import jpabasic.pinnolbe.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
}
