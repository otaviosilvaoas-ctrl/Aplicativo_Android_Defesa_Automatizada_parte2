package com.example.autotarget;

/**
 * Classe Projetil que representa os disparos do canhão.
 * Implementa Runnable para processamento independente.
 */
public class Projetil implements Runnable {
    private double x, y;
    private double raio;
    private double velocidadeX;
    private double velocidadeY;
    private boolean ativo;
    
    // Limites de tela (serão atualizados dinamicamente pelo Jogo)
    private int larguraTela = 1080;
    private int alturaTela = 1920;

    public Projetil(double x, double y, double angulo, double velocidade) {
        this.x = x;
        this.y = y;
        this.raio = 8; 
        this.velocidadeX = velocidade * Math.cos(angulo);
        this.velocidadeY = velocidade * Math.sin(angulo);
        this.ativo = true;
    }

    public void mover() {
        x += velocidadeX;
        y += velocidadeY;

        // Inativa o projétil se sair da tela
        if (x < -raio || x > larguraTela + raio || y < -raio || y > alturaTela + raio) {
            ativo = false;
        }
    }

    @Override
    public void run() {
        while (ativo && !Thread.currentThread().isInterrupted()) {
            mover();
            try {
                // Sincronizado com a taxa de atualização dos alvos (aprox 60 FPS)
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