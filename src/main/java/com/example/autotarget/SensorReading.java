package com.example.autotarget;

/**
 * Representa uma leitura individual de sensor com ruído.
 */
public class SensorReading {
    public final long timestamp;
    public final double x;
    public final double y;
    public final double vx;
    public final double vy;

    public SensorReading(long timestamp, double x, double y, double vx, double vy) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }
}
