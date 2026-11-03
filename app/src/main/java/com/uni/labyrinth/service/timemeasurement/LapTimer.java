package com.uni.labyrinth.service.timemeasurement;

import java.util.concurrent.atomic.AtomicLong;

public final class LapTimer {

    // Speichert den Zeitpunkt des letzten Aufrufs (global für die JVM)
    private static final AtomicLong LAST_NS =
            new AtomicLong(System.nanoTime());

    private LapTimer() {} // keine Instanzen

    /**
     * Gibt die Zeit seit dem vorherigen Aufruf von lap() aus.
     * Funktioniert unabhängig davon, wo sie aufgerufen wird.
     */
    public static void lap() {
        lap("lap");
    }
    public static void reset() {
        long now = System.nanoTime();
    }
    public static void lap(String label) {
        long now = System.nanoTime();
        long previous = LAST_NS.getAndSet(now);

        double ms = (now - previous) / 1_000_000.0;
        System.out.printf("[LapTimer:%s] Δ %.3f ms%n", label, ms);
    }
}
