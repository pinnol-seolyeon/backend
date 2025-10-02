package jpabasic.pinnolbe.domain;

public enum Status {

    ACTIVE, //학습 중
    INACTIVE, //이벤트 인식 안됨
    COMPLETED, //학습 완료
    EXITED //강제 종료 (timeout)
}
