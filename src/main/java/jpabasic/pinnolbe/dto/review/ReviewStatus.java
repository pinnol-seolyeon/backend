package jpabasic.pinnolbe.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ReviewStatus {
    LOCKED("아직 복습을 시작할 수 없습니다."),
    UNLOCKED("복습이 가능하지만 아직 완료하지 않았습니다."),
    COMPLETED("복습을 완료했습니다.");

    private String description;

}
