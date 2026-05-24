package com.example.autotarget;

import java.util.Random;

public class AlvoRapido extends Alvo {

    private Random random;
    private double velocidadeX;
    private double velocidadeY;
    private int contadorMudanca;
    private static final int INTERVALO_MUDANCA = 100;

    public AlvoRapido(double x, double y, double raio, double velocidade) {
        super(x, y, raio, velocidade * 1.5);
        this.random = new Random();
        this.contadorMudanca = 0;
        inicializarDirecao();
    }

    private void inicializarDirecao() {
        double angulo = random.nextDouble() * 2 * Math.PI;
        this.velocidadeX = velocidade * Math.cos(angulo);
        this.velocidadeY = velocidade * Math.sin(angulo);
    }

    @Override
    public void mover() {
        contadorMudanca++;
        if (contadorMudanca >= INTERVALO_MUDANCA) {
            inicializarDirecao();
            contadorMudanca = 0;
        }

        x += velocidadeX;
        y += velocidadeY;

        if (larguraTela > 0) {
            if (x - raio < 0) {
                velocidadeX = Math.abs(velocidadeX);
                x = raio;
            } else if (x + raio > larguraTela) {
                velocidadeX = -Math.abs(velocidadeX);
                x = larguraTela - raio;
            }
        }

        if (alturaTela > 0) {
            if (y - raio <= 0) {
                velocidadeY = Math.abs(velocidadeY);
                y = raio;
            } else if (y + raio >= alturaTela) {
                velocidadeY = -Math.abs(velocidadeY);
                y = alturaTela - raio;
            }
        }
    }

    @Override
    public double getVelocidadeX() { return velocidadeX; }

    @Override
    public double getVelocidadeY() { return velocidadeY; }
}
