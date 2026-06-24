package com.example.autotarget;

/**
 * AV3 Letra D: Classe que representa os dados de telemetria do sistema (Sistemas Ciberfísicos).
 */
public class Telemetria {
    private float temperatura;
    private long timestamp;

    public Telemetria() {
    }

    public Telemetria(float temperatura, long timestamp) {
        this.temperatura = temperatura;
        this.timestamp = timestamp;
    }

    public float getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(float temperatura) {
        this.temperatura = temperatura;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
