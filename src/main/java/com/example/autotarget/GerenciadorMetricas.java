package com.example.autotarget;

import android.util.Log;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utilitário para monitoramento de desempenho e concorrência.
 */
public class GerenciadorMetricas {
    public static final boolean DEBUG = false; 
    private static final String TAG = "JogoMetricas";

    private static final AtomicInteger totalColisoes = new AtomicInteger(0);
    private static long tempoMedioLoop = 0;
    private static int fpsAtual = 0;
    private static long somaFps = 0;
    private static int contagemFps = 0;
    private static int picoThreads = 0;

    public static void registrarColisao() {
        totalColisoes.incrementAndGet();
    }

    public static void registrarTempoLoop(long tempoMs) {
        tempoMedioLoop = (tempoMedioLoop == 0) ? tempoMs : (tempoMedioLoop + tempoMs) / 2;
    }

    public static void registrarFPS(int fps) {
        fpsAtual = fps;
        somaFps += fps;
        contagemFps++;
    }

    public static int getAverageFps() {
        return contagemFps == 0 ? 0 : (int) (somaFps / contagemFps);
    }

    public static void atualizarPicoThreads(int threads) {
        if (threads > picoThreads) picoThreads = threads;
    }

    public static int getPicoThreads() {
        return picoThreads;
    }

    public static void reset() {
        totalColisoes.set(0);
        tempoMedioLoop = 0;
        somaFps = 0;
        contagemFps = 0;
        picoThreads = 0;
    }

    public static void log(String acao, String detalhes) {
        if (DEBUG) {
            Log.d(TAG, String.format("[%d] %s: %s", System.currentTimeMillis(), acao, detalhes));
        }
    }

    public static void logEstado(int alvos, int canhoes, int projeteis) {
        atualizarPicoThreads(alvos + canhoes + projeteis + 5);
        if (DEBUG) {
            Log.i(TAG, String.format("ESTADO | FPS: %d | Loop: %dms | Alvos: %d | Canhões: %d | Projéteis: %d | Colisões: %d",
                    fpsAtual, tempoMedioLoop, alvos, canhoes, projeteis, totalColisoes.get()));
        }
    }
}
