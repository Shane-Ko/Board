package io.github.shane_ko.board.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    //존재하지 않는 게시글 조회 예외
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND,"요청하신 게시글을 찾을 수 없습니다."),
    //서버 오류 (FallBack 용)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    //입력값 검증 관련 예외
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,"입력값이 올바르지 않습니다"),
    //존재하지 않는 댓글 조회 예외
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND,"요청하신 댓글을 찾을 수 없습니다."),
    //회원아이디 중복 예외
    DUPLICATE_USERNAME(HttpStatus.CONFLICT,"이미 존재하는 아이디 입니다"),
    //회원닉네임 중복 예외
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT,"이미 존재하는 닉네임 입니다"),
    // 아이디 혹은 비밀번호 불일치 예외
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED,"아이디 또는 비밀번호가 일치하지 않습니다"),
    // 존재하지 않는 사용자 조회 예외
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    // 인가 실패 예외 (Authorization) - 로그인은 했지만 권한 없음 (403)
    FORBIDDEN(HttpStatus.FORBIDDEN,"접근 권한이 없습니다"),
    // 인증 실패 예외 (Authentication) - 누구인지 확인 불가 (401)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");

    private final HttpStatus status;
    private final String message;
    
    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
