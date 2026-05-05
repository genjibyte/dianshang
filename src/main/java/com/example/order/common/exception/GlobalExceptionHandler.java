package com.example.order.common.exception;

import com.example.order.common.response.ApiResponse;
import com.example.order.common.response.ResponseCode;
import com.example.order.common.util.TraceIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getResponseCode().getCode(), e.getMessage());
        return withTrace(ApiResponse.error(e.getResponseCode().getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return withTrace(ApiResponse.error(ResponseCode.BAD_REQUEST, message));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ApiResponse<Void> handleMissingHeader(MissingRequestHeaderException e) {
        log.warn("缺少请求头: {}", e.getHeaderName());
        return withTrace(ApiResponse.error(ResponseCode.BAD_REQUEST, "缺少请求头: " + e.getHeaderName()));
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return withTrace(ApiResponse.error(ResponseCode.INTERNAL_ERROR));
    }

    private <T> ApiResponse<T> withTrace(ApiResponse<T> response) {
        response.setTraceId(TraceIdUtil.get());
        return response;
    }
}
