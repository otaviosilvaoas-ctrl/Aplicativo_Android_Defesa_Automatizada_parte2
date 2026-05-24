package com.example.autotarget;

import java.util.List;
import java.util.ArrayList;

/**
 * Classe Canhao que gerencia mira e disparos.
 * Implementa Runnable para processamento independente da UI.
 */
public class Canhao implements Runnable {
    private double x, y;
    private double targetX, targetY; 
    private double angulo;
    private final List<Projetil> projeteis;
    private boolean ativo;
    private final Jogo jogo;
    private final int id;
    
    private static final double VELOCIDADE_PROJETIL = 18;
    private static final int INTERVALO_DE_DISPARO_BASE = 700;
    private static final double VELOCIDADE_MOVIMENTO = 2.0; 

    public Canhao(double x, double y, Jogo jogo, int id) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.jogo = jogo;
        this.id = id;
        this.projeteis = new ArrayList<>();
        this.ativo = true;
    }

    public synchronized void setPosicaoObjetivo(double tx, double ty) {
        this.targetX = tx;
        this.targetY = ty;
    }

    private void atualizarMovimento() {
        double dx = targetX - x;
        double dy = targetY - y;
        double dist = Math.hypot(dx, dy);
        
        if (dist > VELOCIDADE_MOVIMENTO) {
            x += (dx / dist) * VELOCIDADE_MOVIMENTO;
            y += (dy / dist) * VELOCIDADE_MOVIMENTO;
        } else {
            x = targetX;
            y = targetY;
        }
    }

    public synchronized void mirar() {
        List<Alvo> alvos = jogo.getAlvos();
        Alvo alvoMaisProximo = null;
        double menorDistanciaSq = Double.MAX_VALUE;
        
        double centroX = jogo.getLarguraTela() / 2.0;
        boolean canhaoLadoEsquerdo = this.x < centroX;

        for (Alvo a : alvos) {
            if (a.isAtivo()) {
                boolean alvoLadoEsquerdo = a.getX() < centroX;
                if (canhaoLadoEsquerdo == alvoLadoEsquerdo) {
                    double dx = a.getX() - this.x;
                    double dy = a.getY() - this.y;
                    double distSq = dx * dx + dy * dy;
                    if (distSq < menorDistanciaSq) {
                        menorDistanciaSq = distSq;
                        alvoMaisProximo = a;
                    }
                }
            }
        }

        if (alvoMaisProximo != null) {
            this.angulo = Math.atan2(alvoMaisProximo.getY() - this.y, alvoMaisProximo.getX() - this.x);
        }
    }

    public void disparar() throws JogoException {
        if (!ativo) return;
        if (jogo.consumirEnergia(this.x)) {
            Projetil p = new Projetil(x, y, angulo, VELOCIDADE_PROJETIL);
            synchronized (projeteis) {
                projeteis.add(p);
            }
            jogo.dispararProjetil(p);
        }
    }

    @Override
    public void run() {
        long lastShot = 0;
        String taskId = (this.x < jogo.getLarguraTela() / 2.0) ? "T2" : "T3";

        while (ativo && !Thread.currentThread().isInterrupted()) {
            atualizarMovimento();
            mirar();
            
            long now = System.currentTimeMillis();
            boolean esquerda = this.x < (jogo.getLarguraTela() / 2.0);
            double penalidade = jogo.getPenalidadeLado(esquerda);
            long intervaloFinal = (long) (INTERVALO_DE_DISPARO_BASE * (1 + penalidade));

            if (now - lastShot >= intervaloFinal) {
                // T2/T3: Monitoramento de Disparo
                long startTime = System.currentTimeMillis();
                RealTimeScheduler.startTask(taskId);
                
                try {
                    disparar();
                    lastShot = now;
                } catch (JogoException e) {
                    e.printStackTrace();
                }
                
                RealTimeScheduler.endTask(taskId, startTime);
            }

            try {
                Thread.sleep(20); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public List<Projetil> getProjeteis() {
        synchronized (projeteis) {
            return new ArrayList<>(projeteis);
        }
    }

    public void limparProjeteis() {
        synchronized (projeteis) {
            projeteis.removeIf(p -> !p.isAtivo());
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngulo() { return angulo; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public void mover(double nx, double ny, double na) { 
        this.x = nx; 
        this.y = ny; 
        this.targetX = nx;
        this.targetY = ny;
        this.angulo = na; 
    }
    public int getId() { return id; }
}
