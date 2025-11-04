package jpabasic.pinnolbe.global;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    NO_COOKIE(HttpStatus.UNAUTHORIZED,"TOKEN-001","REFRESH TOKEN 만료. 재로그인하세요."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,"TOKEN-002","ACCESS TOKEN이 만료되었습니다."),
    REISSUE_TOKEN(HttpStatus.UNAUTHORIZED,"TOKEN-003","access token을 재발급 받으세요."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"TOKEN-004","refresh token이 만료되었습니다. 재로그인하세요."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST,"TOKEN-004","토큰이 유효하지 않습니다."),

    //user 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"USER-001","해당 유저를 찾을 수 없어요."),

    //STUDY 관련
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND,"STUDY-001","해당 유저의 공부 기록을 찾을 수 없어요."),

    //BOOK 관련
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND,"BOOK-001","해당 ID의 책을 찾을 수 없어요."),

    //redis 관련
    REDIS_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"Redis-001","redis에 저장 실패했어요."),

    // study session 관련
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND,"session-001","기존의 study session 기록이 없어요."),
    STUDY_SESSION_LOG_NOT_FOUND(HttpStatus.NOT_FOUND,"session-002","StudySessionLog 엔티티가 없어요."),
    SESSION_LOG_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"session-003","세션 로그 삭제에 오류가 발생했어요."),

    //chapter 관련
    CHAPTER_NOT_FOUND(HttpStatus.NOT_FOUND,"chapter-001","해당 chapter내용을 찾을 수 없어요."),

    //weekly-analysis 관련
    WEEKLY_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND,"weekly-analysis-001","해당 weekly analysis를 찾을 수 없어요.(새로 생성해야)"),

    //결제 관련
    PAYMENT_ERROR(HttpStatus.NOT_FOUND,"payment-001","결제 정보를 찾을 수 없습니다."),
    PAYMENT_ERROR_ORDER_PRICE(HttpStatus.BAD_REQUEST,"payment-002","요청 금액이 실제 금액과 일치하지 않습니다."),
    PAYMENT_ERROR_ORDER_PAY_TYPE(HttpStatus.BAD_REQUEST,"payment-003","결제 타입이 올바르지 않습니다."),
    PAYMENT_ERROR_ORDER_NAME(HttpStatus.BAD_REQUEST,"payment-004","결제 이름이 올바르지 않습니다."),

    //DB관련
    DB_ERROR_SAVE(HttpStatus.INTERNAL_SERVER_ERROR,"database-001","데이터 베이스 저장 과정에서 오류가 발생했어요.");

    private final HttpStatus status;
    private final String code;
    private final String message;



}
