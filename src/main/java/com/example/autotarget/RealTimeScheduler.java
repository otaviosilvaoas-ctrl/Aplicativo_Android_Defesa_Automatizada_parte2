package com.example.autotarget;

import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
        // T1: Movimentação dos Alvos (Prioridade 1 - Maior)
        tasks.put("T1", new TaskMetrics("T1", "Movimento Alvos", 1, 16, 16));
        // T2: Disparo Canhões Esquerda
        tasks.put("T2", new TaskMetrics("T2", "Disparo Esq", 2, 700, 700));
        // T3: Disparo Canhões Direita
        tasks.put("T3", new TaskMetrics("T3", "Disparo Dir", 2, 700, 700));
        // T4: Verificação de Colisões
        tasks.put("T4", new TaskMetrics("T4", "Colisões", 1, 20, 20));
        // T5: UI / Renderização
        tasks.put("T5", new TaskMetrics("T5", "Renderização", 1, 16, 16));
        // T6: Coleta Sensores
        tasks.put("T6", new TaskMetrics("T6", "Coleta Sensores", 3, 1000, 1000));
        // T7: Reconciliação e Otimização
        tasks.put("T7", new TaskMetrics("T7", "Reconciliação", 4, 10000, 10000));
        // T8: Energia e Penalidades
        tasks.put("T8", new TaskMetrics("T8", "Gerenc. Energia", 3, 100, 100));
    }

    public static void startTask(String id) {
        TaskMetrics tm = tasks.get(id);
        if (tm != null) tm.recordStart();
    }

    public static void endTask(String id, long startTime) {
        TaskMetrics tm = tasks.get(id);
        if (tm != null) {
            tm.recordEnd(startTime);
        }
    }

    public static TaskMetrics getMetrics(String id) {
        return tasks.get(id);
    }

    public static List<TaskMetrics> getAllMetrics() {
        List<TaskMetrics> list = new ArrayList<>(tasks.values());
        Collections.sort(list, Comparator.comparing(m -> m.id));
        return list;
    }

    public static int getTotalDeadlinesMissed() {
        int total = 0;
        for (TaskMetrics tm : tasks.values()) {
            total += tm.getDeadlinesMissed();
        }
        return total;
    }

    public static void resetAll() {
        for (TaskMetrics tm : tasks.values()) {
            tm.reset();
        }
    }

    public static void setNumCores(int n) {
        numCores = n;
    }

    public static int getNumCores() {
        return numCores;
    }

    public static double getTotalUtilization() {
        double utilization = 0;
        for (TaskMetrics tm : tasks.values()) {
            utilization += tm.getUtilization();
        }
        return utilization;
    }

    public static String getScalabilityAnalysis() {
        double utilization = getTotalUtilization();
        double limit = numCores * (Math.pow(2, 1.0/tasks.size()) - 1);
        
        return String.format(java.util.Locale.US, 
            "Utilização: %.3f / Limite RM: %.3f\nStatus: %s", 
            utilization, limit, (utilization <= limit ? "SISTEMA ESCALONÁVEL" : "SOBRECARGA"));
    }
    
    public static void logReport() {
        Log.i(TAG, "--- RELATÓRIO DE TEMPO REAL ---");
        for (TaskMetrics tm : tasks.values()) {
            Log.i(TAG, String.format(java.util.Locale.US, 
                "Task %s [%s] | Avg Ci: %.2fms | Jitter: %.2fms | Missed: %d",
                tm.id, tm.descricao, tm.getAverageCi(), tm.getAverageJitter(), tm.getDeadlinesMissed()));
        }
    }
}
