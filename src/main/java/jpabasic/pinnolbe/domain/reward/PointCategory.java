package jpabasic.pinnolbe.domain.reward;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointCategory {

    GAME("게임 포인트"),
    MISSION("방문 미션"),
    REFUND("계좌 환급"),
    PURCHASE("상품권 구매");

    private final String description;

}
