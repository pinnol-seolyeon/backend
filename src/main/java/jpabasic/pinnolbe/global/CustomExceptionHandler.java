package jpabasic.pinnolbe.global;

import jpabasic.pinnolbe.global.exception.user.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ApiErrorDetail> handleCustomException(final CustomException e) {
        return ApiErrorDetail.toApiErrorDetail(e.getErrorCode());
    }
}
