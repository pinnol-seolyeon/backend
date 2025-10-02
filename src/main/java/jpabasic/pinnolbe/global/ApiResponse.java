package jpabasic.pinnolbe.global;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String message; //응답 메시지
    private int status; //http 상태 코드
    private T data; //실제 반환할 데이터

    public static <T> ApiResponse<T> success(String message,T data) {
        return new ApiResponse<>(message,200,data);
    }

    public static <T> ApiResponse<T> fail(String message,int status) {
        return new ApiResponse<>(message,status, null);
    }
}
