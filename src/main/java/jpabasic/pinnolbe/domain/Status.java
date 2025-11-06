package jpabasic.pinnolbe.domain;

public enum Status {

    ACTIVE, //학습 중
    INACTIVE, //이벤트 인식 안됨
    COMPLETED, //학습 완료
    EXIT //강제 종료 (timeout-TTL 만료 시 ) & 유저가 학습하기 창에서 나갔을 때
}
