package jpabasic.pinnolbe.global;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;


@Data
@Builder
public class ApiErrorDetail {

    private int status;
    private String name;
    private String code;
    private String message;

    public static ResponseEntity<ApiErrorDetail> toApiErrorDetail(ErrorCode e){
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ApiErrorDetail.builder()
                        .status(e.getHttpStatus().value())
                        .name(e.name())
                        .code(e.getCode())
                        .message(e.getMessage())
                        .build());
    }



}
