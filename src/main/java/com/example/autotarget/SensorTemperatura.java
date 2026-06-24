package com.example.autotarget;

import java.util.Random;

/**
 * AV3 Letra D: Classe que simula um sensor físico de temperatura (Sistemas Ciberfísicos).
 */
public class SensorTemperatura {
    private final Random random = new Random();

    /**
     * Retorna uma temperatura simulada entre 30.0 e 50.0 graus Celsius.
     */
    public float lerTemperatura() {
        return 30.0f + random.nextFloat() * (50.0f - 30.0f);
    }
}
