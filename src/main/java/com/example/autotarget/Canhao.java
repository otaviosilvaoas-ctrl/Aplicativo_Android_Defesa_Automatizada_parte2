package com.example.autotarget;

import java.util.List;
import java.util.ArrayList;

/**
 * Classe Canhao que gerencia mira e disparos.
 * Implementa Runnable para processamento independente da UI.
 */
public class Canhao implements Runnable {
    private double x, y;
    private double targetX, targetY; // AV2: Posição alvo para movimentação suave
    private double angulo;
    private final List<Projetil> projeteis;
    private boolean ativo;
    private final Jogo jogo;
    private final int id;
    
    private static final double VELOCIDADE_PROJETIL = 18;
    private static final int INTERVALO_DE_DISPARO_BASE = 700;
    private static final double VELOCIDADE_MOVIMENTO = 2.0; // Pixels por ciclo

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

    /**
     * Define uma nova posição para o canhão se mover gradualmente.
     */
    public synchronized void setPosicaoObjetivo(double tx, double ty) {
        this.targetX = tx;
        this.targetY = ty;
    }

    /**
     * Move o canhão suavemente em direção à posição objetivo.
     */
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

    /**
     * Mira apenas nos alvos que estão no mesmo lado da tela que o canhão.
     */
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
        while (ativo && !Thread.currentThread().isInterrupted()) {
            atualizarMovimento();
            mirar();
            
            long now = System.currentTimeMillis();
            
            // AV2: Calcula penalidade por excesso de canhões
            boolean esquerda = this.x < (jogo.getLarguraTela() / 2.0);
            double penalidade = jogo.getPenalidadeLado(esquerda);
            long intervaloFinal = (long) (INTERVALO_DE_DISPARO_BASE * (1 + penalidade));

            if (now - lastShot >= intervaloFinal) {
                try {
                    disparar();
                    lastShot = now;
                } catch (JogoException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(20); // Ciclo de atualização de 50 FPS
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
        this.targetX = nx; // Atualiza objetivo se for via UI
        this.targetY = ny;
        this.angulo = na; 
    }
    public int getId() { return id; }
}
