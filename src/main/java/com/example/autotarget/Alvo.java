package com.example.autotarget;

/**
 * Classe abstrata Alvo que define o comportamento base.
 * Implementa Runnable para permitir execução em threads.
 */
public abstract class Alvo implements Runnable {
    protected double x, y;
    protected double raio;
    protected double velocidade;
    protected boolean ativo;
    protected int larguraTela = 1080;
    protected int alturaTela = 1920;

    public Alvo(double x, double y, double raio, double velocidade) {
        this.x = x;
        this.y = y;
        this.raio = raio;
        this.velocidade = velocidade;
        this.ativo = true;
    }

    // Método polimórfico que define como cada alvo se move
    public abstract void mover();

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

    @Override
    public void run() {
        while (ativo && !Thread.currentThread().isInterrupted()) {
            mover();
            try {
                // Sleep reduzido para maior fluidez (aprox 60 FPS)
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