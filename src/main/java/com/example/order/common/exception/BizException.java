package com.example.order.common.exception;

import com.example.order.common.response.ResponseCode;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private final ResponseCode responseCode;

    public BizException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    public BizException(ResponseCode responseCode, String detail) {
        super(responseCode.getMessage() + ": " + detail);
        this.responseCode = responseCode;
    }
}
