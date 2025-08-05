package jpabasic.pinnolbe.global;

import lombok.Builder;
import lombok.Getter;

@Getter
/*ExceptionHandlerFilter 상에서 사용하는 응답 구조*/
public class ErrorDetailResponse {
    private String msg;

    @Builder
    public ErrorDetailResponse(String msg) {
        this.msg = msg;
    }
}
