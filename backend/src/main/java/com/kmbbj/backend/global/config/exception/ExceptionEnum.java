package com.kmbbj.backend.global.config.exception;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum ExceptionEnum {

    // System Exception
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST"),
    ACCESS_DENIED_EXCEPTION(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류 발생"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),

    // Custom Exception
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "유저를 찾지 못했습니다."),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", " 방을 찾지 못했습니다."),
    ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, "ALARM_NOT_FOUND", " 알람을 찾지 못했습니다."),
    TOTAL_BALANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "TOTAL_BALANCE_NOT_FOUND", " 자산을 찾지 못했습니다."),
    UNAUTHORIZED(HttpStatus.NOT_FOUND, "UNAUTHORIZED", " 인증이 되지 않았습니다."),
    INVALID_USER_DETAILS(HttpStatus.NOT_FOUND, "INVALID_USER_DETAILS", " 사용자를 찾지 못했습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "EMAIL_NOT_FOUND", " 이메일을 찾지 못했습니다."),

    // Token Exception
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "토큰이 만료되었습니다."),
    EXPIRED_TOKEN2(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "토큰 유효성 실패"),
    UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "UNSUPPORTED_TOKEN", "지원하지 않는 토큰입니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "TOKEN_NOT_FOUND", "토큰을 찾을 수 없습니다."),
    INVALID_SIGNATURE(HttpStatus.BAD_REQUEST, "INVALID_SIGNATURE", "유효하지 않은 서명입니다."),
    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "ILLEGAL_ARGUMENT", "잘못된 인자입니다."),
    JWT_FILTER_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JWT_FILTER_INTERNAL_ERROR", "JWT 필터 내부 오류입니다."),

    // User
    EXIST_EMAIL(HttpStatus.BAD_REQUEST, "EXIST_EMAIL", "이미 있는 이메일 입니다."),
    NOT_ALLOW_FILED(HttpStatus.BAD_REQUEST, "NOT_ALLOW_FILED", "이메일 혹은 비밀번호 형식이 틀립니다."),
    DIFFERENT_PASSWORD(HttpStatus.BAD_REQUEST, "DIFFERENT_PASSWORD", "비밀번호가 둘이 다릅니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "NOT_FOUND_USER", "사용자가 없습니다."),
    USER_SUSPENDED(HttpStatus.NOT_FOUND, "USER_SUSPENDED", "사용자가 정지되었습니다."),

    // Room
    NOT_ENTRY_ROOM(HttpStatus.NOT_FOUND, "NOT_ENTRY_ROOM", "해당 방 입장기록이 없습니다."),
    NOT_CURRENT_ROOM(HttpStatus.NOT_FOUND, "NOT_CURRENT_ROOM", "해당 방에 참가해있지 않습니다."),
    NOT_IN_ROOM(HttpStatus.NOT_FOUND, "NOT_IN_ROOM", "방에 참가해있지 않습니다."),
    IN_OTHER_ROOM(HttpStatus.CONFLICT, "IN_OTHER_ROOM", "이미 다른 방에 입장해 있습니다."),
    ROOM_FULL(HttpStatus.CONFLICT,"ROOM_FULL","방이 가득 찼습니다."),
    INSUFFICIENT_ASSET(HttpStatus.FORBIDDEN,"INSUFFICIENT_ASSET","방 조건에 알맞는 자산이 부족합니다."),

    // Game Exception
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND, "GAME_NOT_FOUND", "게임을 찾지 못했습니다."),
    ROUND_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUND_NOT_FOUND", "라운드를 찾지 못했습니다."),
    GAME_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "GAME_ALREADY_STARTED", "게임이 이미 시작되었습니다."),
    DUPLICATE_ROUND(HttpStatus.BAD_REQUEST, "DUPLICATE_ROUND", "이미 존재하는 라운드입니다."),
    ROUND_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUND_RESULT_NOT_FOUND", "라운드 결과를 찾지 못했습니다."),

//    ALREADY_IN_CURRENT_ROOM(HttpStatus.),

    // Balance
    BALANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "BALANCE_NOT_FOUND", "자산을 찾지 못했습니다."),

    // Coin
    EXIST_COIN(HttpStatus.BAD_REQUEST, "EXIST_COIN", "이미 있는 코인 입니다."),
    NOT_FOUND_SYMBOL(HttpStatus.BAD_REQUEST, "NOT_FOUND_SYMBOL", "존재하지 않는 코인 코드입니다."),
    UNSUPPORTED_INTERVAL(HttpStatus.BAD_REQUEST, "UNSUPPORTED_INTERVAL", "지원하지 않는 Interval 형식 입니다."),

    // trasaction
    CASSANDRA_SAVE_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "CASSANDRA_SAVE_EXCEPTION", "Cassandra에 데이터를 저장하는 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private String message;

    ExceptionEnum(HttpStatus status, String code) {
        this.status = status;
        this.code = code;
    }

    ExceptionEnum(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
