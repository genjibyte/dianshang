package com.example.order.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class IdGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private IdGenerator() {}

    public static String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(FORMATTER) + randomSuffix();
    }

    public static String generatePaymentNo() {
        return "PAY" + LocalDateTime.now().format(FORMATTER) + randomSuffix();
    }

    private static String randomSuffix() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    }
}
