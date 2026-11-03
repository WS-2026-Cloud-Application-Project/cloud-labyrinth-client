package com.uni.labyrinth.service.timemeasurement;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class TimeParsing {

    private TimeParsing() {}

    /** Parst Zeitstring zu Instant. Falls keine Zone dabei ist -> UTC annehmen. */
    public static Instant parseToInstantUtc(String s) {
        if (s == null) throw new IllegalArgumentException("time string is null");
        s = s.trim();
        if (s.isEmpty()) throw new IllegalArgumentException("time string is empty");

        // 1) UTC mit Z
        if (s.endsWith("Z")) {
            return Instant.parse(s);
        }

        // 2) Offset vorhanden? (+01:00 oder -05:00 oder +0100 oder +01)
        boolean hasOffset =
                s.matches(".*[+-]\\d\\d:\\d\\d$") ||
                        s.matches(".*[+-]\\d\\d\\d\\d$") ||
                        s.matches(".*[+-]\\d\\d$");

        if (hasOffset) {
            // Normaler ISO Offset
            try {
                return OffsetDateTime.parse(s).toInstant();
            } catch (DateTimeParseException ignored) {}

            // Fallback +HHmm
            try {
                DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.n]XX");
                return OffsetDateTime.parse(s, f).toInstant();
            } catch (DateTimeParseException ignored) {}

            // Fallback +HH
            try {
                DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.n]X");
                return OffsetDateTime.parse(s, f).toInstant();
            } catch (DateTimeParseException ignored) {}

            throw new IllegalArgumentException("Unsupported offset datetime format: " + s);
        }

        // 3) Kein Offset/keine Zone -> als UTC interpretieren
        try {
            LocalDateTime ldt = LocalDateTime.parse(s);
            return ldt.toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Unsupported datetime format: " + s, e);
        }
    }
}