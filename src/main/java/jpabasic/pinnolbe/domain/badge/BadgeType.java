package jpabasic.pinnolbe.domain.badge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BadgeType {

    MODEL_STUDENT("모범생","복습 3회차 완료"),
    SMART_GAMER("스마트 게이머","퀴즈 모두 맞았을 때"),
    SPEED_HUNTER("스피드 사냥꾼","무당벌레 모두 2초 이내 클릭 성공"),
    FINE_HUNTER("정교한 사냥꾼","무당벌레 연속 3마리 클릭 성공");

    private final String name;
    private final String description;
}
