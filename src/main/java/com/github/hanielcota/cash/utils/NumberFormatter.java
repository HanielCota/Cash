package com.github.hanielcota.cash.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NumberFormatter {
    private static final String[] NUMBER_SUFFIXES = {"", "K", "M", "B", "T", "Q", "L"};
    private static final double THOUSAND = 1000.0;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private static final ThreadLocal<DecimalFormat> THREAD_LOCAL_DECIMAL_FORMAT =
            ThreadLocal.withInitial(() -> new DecimalFormat("#.##"));

    public static String formatAbbreviated(double value) {
        int suffixIndex = 0;
        while (value >= THOUSAND && suffixIndex < NUMBER_SUFFIXES.length - 1) {
            value /= THOUSAND;
            suffixIndex++;
        }

        return DECIMAL_FORMAT.format(value) + NUMBER_SUFFIXES[suffixIndex];
    }

    public static String formatAbbreviatedThreadSafe(double value) {
        int suffixIndex = 0;
        while (value >= THOUSAND && suffixIndex < NUMBER_SUFFIXES.length - 1) {
            value /= THOUSAND;
            suffixIndex++;
        }

        try {
            return THREAD_LOCAL_DECIMAL_FORMAT.get().format(value) + NUMBER_SUFFIXES[suffixIndex];
        } finally {
            THREAD_LOCAL_DECIMAL_FORMAT.remove();
        }
    }
}
