package com.example.order.common.util;

import org.slf4j.MDC;

import java.util.UUID;

public final class TraceIdUtil {

    public static final String TRACE_ID_KEY = "traceId";

    private TraceIdUtil() {}

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void set(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    public static String get() {
        return MDC.get(TRACE_ID_KEY);
    }

    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}
