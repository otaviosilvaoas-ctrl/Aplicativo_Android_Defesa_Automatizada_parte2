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
    private int energia;
    
    private static final double VELOCIDADE_PROJETIL = 18;
    private static final int INTERVALO_DE_DISPARO = 700;
    private static final int ENERGIA_MAXIMA = 100;

    public Canhao(double x, double y, Jogo jogo, int id) {
        this.x = x;
        this.y = y;
        this.jogo = jogo;
        this.id = id;
        this.energia = ENERGIA_MAXIMA;
        this.projeteis = new ArrayList<>();
        this.ativo = true;
    }

    public synchronized void mirar() {
        List<Alvo> alvos = jogo.getAlvos();
        Alvo alvoMaisProximo = null;
        double menorDistanciaSq = Double.MAX_VALUE;

        for (Alvo a : alvos) {
            if (a.isAtivo()) {
                double dx = a.getX() - this.x;
                double dy = a.getY() - this.y;
                double distSq = dx * dx + dy * dy;
                if (distSq < menorDistanciaSq) {
                    menorDistanciaSq = distSq;
                    alvoMaisProximo = a;
                }
            }
        }

        if (alvoMaisProximo != null) {
            this.angulo = Math.atan2(alvoMaisProximo.getY() - this.y, alvoMaisProximo.getX() - this.x);
        }
    }

    /**
     * Cria um novo projetil e solicita ao jogo que o execute usando o pool de threads.
     */
    public void disparar() throws JogoException {
        if (!ativo) return;
        
        // Simulação de consumo de energia
        if (energia > 0) {
            Projetil p = new Projetil(x, y, angulo, VELOCIDADE_PROJETIL);
            synchronized (projeteis) {
                projeteis.add(p);
            }
            jogo.dispararProjetil(p);
            // energia -= 1; // Opcional: descomentar para habilitar consumo
        }
    }

    @Override
    public void run() {
        while (ativo && !Thread.currentThread().isInterrupted()) {
            mirar();
            try {
                disparar();
                Thread.sleep(INTERVALO_DE_DISPARO);
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
    public int getEnergia() { return energia; }
    public int getEnergiaMaxima() { return ENERGIA_MAXIMA; }
}
