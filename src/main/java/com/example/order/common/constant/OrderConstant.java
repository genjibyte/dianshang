package com.example.order.common.constant;

public final class OrderConstant {

    private OrderConstant() {}

    public static final String HEADER_IDEMPOTENCY_KEY = "Idempotency-Key";
    public static final String HEADER_USER_ID = "X-User-Id";

    public static final String BIZ_TYPE_CREATE_ORDER = "CREATE_ORDER";
    public static final String BIZ_TYPE_PAY_ORDER = "PAY_ORDER";

    public static final String ORDER_NO_PREFIX = "ORD";
    public static final String PAYMENT_NO_PREFIX = "PAY";
}
