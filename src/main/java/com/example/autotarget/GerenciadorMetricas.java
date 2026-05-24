package com.example.autotarget;

import android.util.Log;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utilitário para monitoramento de desempenho e concorrência.
 */
public class GerenciadorMetricas {
    public static final boolean DEBUG = false; // Modo DEBUG desativado por padrão
    private static final String TAG = "JogoMetricas";

    private static final AtomicInteger totalColisoes = new AtomicInteger(0);
    private static long tempoMedioLoop = 0;
    private static int fpsAtual = 0;

    public static void registrarColisao() {
        totalColisoes.incrementAndGet();
    }

    public static void registrarTempoLoop(long tempoMs) {
        // Média móvel simples para o tempo de loop
        tempoMedioLoop = (tempoMedioLoop == 0) ? tempoMs : (tempoMedioLoop + tempoMs) / 2;
    }

    public static void registrarFPS(int fps) {
        fpsAtual = fps;
    }

    public static void log(String acao, String detalhes) {
        if (DEBUG) {
            Log.d(TAG, String.format("[%d] %s: %s", System.currentTimeMillis(), acao, detalhes));
        }
    }

    public static void logEstado(int alvos, int canhoes, int projeteis) {
        if (DEBUG) {
            Log.i(TAG, String.format("ESTADO | FPS: %d | Loop: %dms | Alvos: %d | Canhões: %d | Projéteis: %d | Colisões: %d",
                    fpsAtual, tempoMedioLoop, alvos, canhoes, projeteis, totalColisoes.get()));
        }
    }
}
