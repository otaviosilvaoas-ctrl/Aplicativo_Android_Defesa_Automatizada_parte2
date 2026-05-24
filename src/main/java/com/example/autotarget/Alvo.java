package com.example.autotarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe abstrata Alvo que define o comportamento base.
 * Implementa Runnable para permitir execução em threads.
 */
public abstract class Alvo implements Runnable {
    protected double x, y;
    protected double raio;
    protected double velocidade;
    protected boolean ativo;
    protected int larguraTela;
    protected int alturaTela;

    // AV2: Buffer de leituras de sensor (Thread-safe)
    private final List<SensorReading> bufferLeituras = Collections.synchronizedList(new ArrayList<>());
    private static final int TAMANHO_MAX_BUFFER = 20;

    public Alvo(double x, double y, double raio, double velocidade) {
        this.x = x;
        this.y = y;
        this.raio = raio;
        this.velocidade = velocidade;
        this.ativo = true;
    }

    public abstract void mover();
    public abstract double getVelocidadeX();
    public abstract double getVelocidadeY();

    /**
     * Lógica de colisão otimizada.
     */
    public boolean verificarColisao(Projetil projetil) {
        double dx = this.x - projetil.getX();
        double dy = this.y - projetil.getY();
        double distanciaSq = dx * dx + dy * dy;
        double raioSoma = this.raio + projetil.getRaio();
        return distanciaSq < raioSoma * raioSoma;
    }

    // AV2: Métodos para o SensorManager
    public void adicionarLeitura(SensorReading leitura) {
        bufferLeituras.add(leitura);
        if (bufferLeituras.size() > TAMANHO_MAX_BUFFER) {
            bufferLeituras.remove(0);
        }
    }

    public List<SensorReading> getHistoricoLeituras() {
        return new ArrayList<>(bufferLeituras);
    }

    @Override
    public void run() {
        while (ativo && !Thread.currentThread().isInterrupted()) {
            // T1: Monitoramento de Movimentação dos Alvos
            long startTime = System.currentTimeMillis();
            RealTimeScheduler.startTask("T1");
            
            mover();
            
            RealTimeScheduler.endTask("T1", startTime);

            try {
                Thread.sleep(16); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRaio() { return raio; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public void setLimitesTela(int w, int h) { this.larguraTela = w; this.alturaTela = h; }
}
