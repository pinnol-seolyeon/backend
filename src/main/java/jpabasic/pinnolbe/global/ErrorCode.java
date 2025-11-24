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
    STUDY_SESSION_ID_NOT_FOUND(HttpStatus.NOT_FOUND,"session-004","user필드에 저장되어있던 sessionId로 찾아봤지만 해당 세션을 DB에서 찾을 수 없어요."),

    //chapter 관련
    CHAPTER_NOT_FOUND(HttpStatus.NOT_FOUND,"chapter-001","해당 chapter내용을 찾을 수 없어요."),

    //weekly-analysis 관련
    WEEKLY_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND,"weekly-analysis-001","해당 weekly analysis를 찾을 수 없어요.(새로 생성해야)"),
    LAST_WEEK_WEEKLY_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND,"weekly-analysis-002","지난 주 학습 기록이 없어요."),

    //badge 관련
    BADGE_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"badge-001","뱃지 저장에 오류가 발생했어요."),
    BADGE_NOT_FOUND(HttpStatus.NOT_FOUND,"badge-002","뱃지를 찾을 수 없어요."),

    //학습 현황 관련
    CURRENT_SITUATION_NOT_FOUND(HttpStatus.NOT_FOUND,"current-situation-001","학습한 내용이 없어서 학습 현황을 불러올 수 없어요."),

    //quiz 관련
    QUIZ_NOTES_NOT_FOUND(HttpStatus.NOT_FOUND,"quiz-notes-001","해당 유저, 챕터에 대한 quiz notes를 찾을 수 없어요."),
    QUIZ_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND,"quiz-records-001","quiz record를 찾을 수 없어요."),
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND,"quiz-001","해당 quiz를 찾을 수 없어요."),
    QUIZ_NOT_SOLVED_YET(HttpStatus.NOT_FOUND,"quiz-002","아직 해당 단원에 풀이한 퀴즈가 없어요."),

    //ChapterProgress 관련
    CHAPTER_PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND,"chapter-progress-001","해당 chapter progress를 찾을 수 없어요."),

    //Review 관련
    REVIEW_ALREADY_COMPLETED(HttpStatus.CONFLICT,"review-001","이미 복습된 복습이에요"),
    FIRST_REVIEW_NOT_COMPLETED(HttpStatus.BAD_REQUEST,"review-002","1차 복습이 완료되지 않아 2차 복습을 할 수 없어요."),


    //결제 관련
    PAYMENT_ERROR(HttpStatus.NOT_FOUND,"payment-001","결제 정보를 찾을 수 없습니다."),
    PAYMENT_ERROR_ORDER_PRICE(HttpStatus.BAD_REQUEST,"payment-002","요청 금액이 실제 금액과 일치하지 않습니다."),
    PAYMENT_ERROR_ORDER_PAY_TYPE(HttpStatus.BAD_REQUEST,"payment-003","결제 타입이 올바르지 않습니다."),
    PAYMENT_ERROR_ORDER_NAME(HttpStatus.BAD_REQUEST,"payment-004","결제 이름이 올바르지 않습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND,"payment-005","해당 결제 정보가 없습니다."),
    PAYMENT_CANCEL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"payment-006", "결제 취소 중 오류가 발생했어요."),
    PAYMENT_ERROR_ORDER_NOTFOUND(HttpStatus.NOT_FOUND,"payment-007", "해당 주문을 찾을 수가 없어요."),

    //DB관련
    DB_ERROR_SAVE(HttpStatus.INTERNAL_SERVER_ERROR,"database-001","데이터 베이스 저장 과정에서 오류가 발생했어요.");

    private final HttpStatus status;
    private final String code;
    private final String message;



}
