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
    
    private static final double VELOCIDADE_PROJETIL = 18;
    private static final int INTERVALO_DE_DISPARO = 700;

    public Canhao(double x, double y, Jogo jogo) {
        this.x = x;
        this.y = y;
        this.jogo = jogo;
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

    public void disparar() throws JogoException {
        if (!ativo) return;
        Projetil p = new Projetil(x, y, angulo, VELOCIDADE_PROJETIL);
        synchronized (projeteis) {
            projeteis.add(p);
        }
        // Inicia a tarefa do projétil em uma nova thread
        new Thread(p).start();
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
}