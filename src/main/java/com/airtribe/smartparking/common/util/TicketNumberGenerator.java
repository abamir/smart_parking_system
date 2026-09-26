package com.airtribe.smartparking.common.util;

import java.util.UUID;

public final class TicketNumberGenerator {


    private TicketNumberGenerator() {
    }

    public static String generate() {

        return "TKT-"
                + System.currentTimeMillis()
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 5)
                .toUpperCase();
    }
}
