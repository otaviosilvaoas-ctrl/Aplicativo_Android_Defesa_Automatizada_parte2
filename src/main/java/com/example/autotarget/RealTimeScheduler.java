package com.example.autotarget;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gerenciador de Escalonamento e Análise de Tempo Real.
 */
public class RealTimeScheduler {
    private static final String TAG = "RTScheduler";
    private static final Map<String, TaskMetrics> tasks = new HashMap<>();
    private static final AtomicInteger totalDeadlinesMissed = new AtomicInteger(0);
    private static int numCores = Runtime.getRuntime().availableProcessors();

    static {
        // T1: Movimentação dos Alvos (60 FPS target)
        tasks.put("T1", new TaskMetrics("T1", "Movimento Alvos", 16, 16));
        // T2: Disparo Canhões Esquerda
        tasks.put("T2", new TaskMetrics("T2", "Disparo Esq", 700, 700));
        // T3: Disparo Canhões Direita
        tasks.put("T3", new TaskMetrics("T3", "Disparo Dir", 700, 700));
        // T4: Verificação de Colisões
        tasks.put("T4", new TaskMetrics("T4", "Colisões", 20, 20));
        // T5: UI / Renderização
        tasks.put("T5", new TaskMetrics("T5", "Renderização", 16, 16));
        // T6: Coleta Sensores
        tasks.put("T6", new TaskMetrics("T6", "Coleta Sensores", 1000, 1000));
        // T7: Reconciliação e Otimização
        tasks.put("T7", new TaskMetrics("T7", "Reconciliação", 10000, 10000));
        // T8: Energia e Penalidades
        tasks.put("T8", new TaskMetrics("T8", "Gerenc. Energia", 100, 100));
    }

    public static void startTask(String id) {
        TaskMetrics tm = tasks.get(id);
        if (tm != null) tm.recordStart();
    }

    public static void endTask(String id, long startTime) {
        TaskMetrics tm = tasks.get(id);
        if (tm != null) {
            tm.recordEnd(startTime);
            if (System.currentTimeMillis() - startTime > tm.deadline) {
                totalDeadlinesMissed.incrementAndGet();
            }
        }
    }

    public static TaskMetrics getMetrics(String id) {
        return tasks.get(id);
    }

    public static int getTotalDeadlinesMissed() {
        return totalDeadlinesMissed.get();
    }

    public static void setNumCores(int n) {
        numCores = n;
    }

    public static int getNumCores() {
        return numCores;
    }

    /**
     * Análise Rate Monotonic e Utilização.
     */
    public static String getScalabilityAnalysis() {
        double utilization = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("Analise RM (Cores: ").append(numCores).append("):\n");
        
        for (TaskMetrics tm : tasks.values()) {
            double u = tm.getUtilization();
            utilization += u;
            sb.append(String.format(java.util.Locale.US, "%s: U=%.3f, Ri=%dms, WCET=%dms\n", 
                    tm.id, u, tm.getRi(), tm.getWcet()));
        }
        
        double limit = numCores * (Math.pow(2, 1.0/tasks.size()) - 1); // Simplificado para N cores
        sb.append(String.format(java.util.Locale.US, "Utilização Total: %.3f / Limite RM: %.3f\n", utilization, limit));
        sb.append(utilization <= limit ? "Sistema Escalonável" : "Risco de Sobrecarga");
        
        return sb.toString();
    }
    
    public static void logReport() {
        Log.i(TAG, "--- RELATÓRIO DE TEMPO REAL ---");
        Log.i(TAG, getScalabilityAnalysis());
        for (TaskMetrics tm : tasks.values()) {
            Log.i(TAG, String.format(java.util.Locale.US, 
                "Task %s [%s] | Avg Ci: %.2fms | Jitter: %.2fms | Missed: %d",
                tm.id, tm.descricao, tm.getAverageCi(), tm.getAverageJitter(), tm.getDeadlinesMissed()));
        }
    }
}
