package com.example.autotarget;

import java.util.List;
import java.util.ArrayList;

/**
 * Classe Canhao que gerencia mira e disparos.
 * Implementa Runnable para processamento independente da UI.
 */
public class Canhao implements Runnable {
    private double x, y;
    private double angulo;
    private final List<Projetil> projeteis;
    private boolean ativo;
    private final Jogo jogo;
    private final int id;
    
    private static final double VELOCIDADE_PROJETIL = 18;
    private static final int INTERVALO_DE_DISPARO_BASE = 700;

    public Canhao(double x, double y, Jogo jogo, int id) {
        this.x = x;
        this.y = y;
        this.jogo = jogo;
        this.id = id;
        this.projeteis = new ArrayList<>();
        this.ativo = true;
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
                
                // Só mira se o alvo estiver no mesmo lado que o canhão
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

    /**
     * Cria um novo projetil se houver energia disponível no seu lado.
     */
    public void disparar() throws JogoException {
        if (!ativo) return;
        
        // AV2: Consome energia do lado correspondente. Se não houver, não dispara.
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
        while (ativo && !Thread.currentThread().isInterrupted()) {
            mirar();
            try {
                disparar();
                
                // AV2: Calcula penalidade por excesso de canhões
                boolean esquerda = this.x < (jogo.getLarguraTela() / 2.0);
                double penalidade = jogo.getPenalidadeLado(esquerda);
                long intervaloFinal = (long) (INTERVALO_DE_DISPARO_BASE * (1 + penalidade));
                
                Thread.sleep(intervaloFinal);
            } catch (JogoException e) {
                e.printStackTrace();
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
    public void mover(double nx, double ny, double na) { this.x = nx; this.y = ny; this.angulo = na; }
    public int getId() { return id; }
    
    // Removidos métodos de energia interna pois agora a energia é gerenciada pelo Jogo (por lado)
}
