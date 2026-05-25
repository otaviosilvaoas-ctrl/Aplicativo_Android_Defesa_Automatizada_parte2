package com.example.autotarget;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Modelo de dados para análise de tempo real de uma tarefa (Ti).
 */
public class TaskMetrics {
    public final String id;
    public final String descricao;
    public final int prioridade;
    public final long period; // Pi (ms)
    public final long deadline; // Di (ms)

    private long lastStartTime = 0;
    private final AtomicLong executionCount = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0); // Soma de Ci
    private final AtomicLong maxExecutionTime = new AtomicLong(0); // WCET
    private final AtomicLong totalJitter = new AtomicLong(0);
    private final AtomicLong deadlinesMissed = new AtomicLong(0);
    private long lastResponseTime = 0;

    public TaskMetrics(String id, String descricao, int prioridade, long period, long deadline) {
        this.id = id;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.period = period;
        this.deadline = deadline;
    }

    public void reset() {
        lastStartTime = 0;
        executionCount.set(0);
        totalExecutionTime.set(0);
        maxExecutionTime.set(0);
        totalJitter.set(0);
        deadlinesMissed.set(0);
        lastResponseTime = 0;
    }

    public void recordStart() {
        long now = System.currentTimeMillis();
        if (lastStartTime != 0) {
            long actualPeriod = now - lastStartTime;
            long jitter = Math.abs(actualPeriod - period);
            totalJitter.addAndGet(jitter);
        }
        lastStartTime = now;
    }

    public void recordEnd(long startTime) {
        long now = System.currentTimeMillis();
        long ci = now - startTime;
        executionCount.incrementAndGet();
        totalExecutionTime.addAndGet(ci);
        
        long currentMax = maxExecutionTime.get();
        while (ci > currentMax && !maxExecutionTime.compareAndSet(currentMax, ci)) {
            currentMax = maxExecutionTime.get();
        }

        // Ri aproximado pelo tempo de execução somado ao jitter médio
        lastResponseTime = ci + (long)getAverageJitter(); 

        if (ci > deadline) {
            deadlinesMissed.incrementAndGet();
        }
    }

    public double getAverageCi() {
        long count = executionCount.get();
        return count == 0 ? 0 : (double) totalExecutionTime.get() / count;
    }

    public long getWcet() { return maxExecutionTime.get(); }
    public long getDeadlinesMissed() { return deadlinesMissed.get(); }
    public double getAverageJitter() {
        long count = executionCount.get();
        return count <= 1 ? 0 : (double) totalJitter.get() / (count - 1);
    }
    public double getUtilization() {
        return (double) getAverageCi() / period;
    }
    
    public long getRi() { return lastResponseTime; }
}
