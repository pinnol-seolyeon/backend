package jpabasic.pinnolbe.global;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    NO_COOKIE(HttpStatus.UNAUTHORIZED,"TOKEN-001","REFRESH TOKEN 만료. 재로그인하세요."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,"TOKEN-002","ACCESS TOKEN이 만료되었습니다."),
    REISSUE_TOKEN(HttpStatus.UNAUTHORIZED,"TOKEN-003","access token을 재발급 받으세요."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"TOKEN-004","refresh token이 만료되었습니다. 재로그인하세요."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST,"TOKEN-004","토큰이 유효하지 않습니다."),

    //STUDY 관련
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND,"STUDY-001","해당 유저의 공부 기록을 찾을 수 없어요."),

    //BOOK 관련
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND,"BOOK-001","해당 ID의 책을 찾을 수 없어요.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
