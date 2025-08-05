package jpabasic.pinnolbe.global.exception.user;

import jpabasic.pinnolbe.global.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException {
    ErrorCode errorCode;
}
