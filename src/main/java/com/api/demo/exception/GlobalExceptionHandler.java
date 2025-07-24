package com.api.demo.exception;

import com.api.demo.model.result.RestError;
import com.api.demo.service.thirdparty.TelegramNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final TelegramNotifier telegramNotifier;

    // if
    @ExceptionHandler(BadRequestRuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public RestError handleBadRequestRuntimeException(BadRequestRuntimeException ex) {
        return new RestError("bad_request", ex.getMessage());
    }

    // else
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)   // 500
    public RestError handleException(Exception ex) {

        // 만약에 30초가 걸려요. 텔레그램 보낼때.
        telegramNotifier.sendMessage("서버 오류 : " + ex.getMessage());
        return new RestError("internal_server_error", ex.getMessage());
    }
}
