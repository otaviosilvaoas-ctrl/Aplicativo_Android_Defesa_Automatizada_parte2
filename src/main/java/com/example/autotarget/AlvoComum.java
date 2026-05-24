package com.example.autotarget;

import java.util.Random;

public class AlvoComum extends Alvo {
    private Random random;
    private double velocidadeX;
    private double velocidadeY;

    public AlvoComum(double x, double y, double raio, double velocidade) {
        super(x, y, raio, velocidade);
        this.random = new Random();
        inicializarDirecao();
    }

    private void inicializarDirecao() {
        double angulo = random.nextDouble() * 2 * Math.PI;
        velocidadeX = velocidade * Math.cos(angulo);
        velocidadeY = velocidade * Math.sin(angulo);
    }

    @Override
    public void mover() {
        // atualiza posição do alvo
        x += velocidadeX;
        y += velocidadeY;

        // Verifica bordas horizontais - Usando as novas variáveis larguraTela e alturaTela
        if (x - raio < 0 || x + raio >= larguraTela) {
            velocidadeX = -velocidadeX;
            x = Math.max(raio, Math.min(larguraTela - raio, x));
        }

        // Verifica bordas verticais
        if (y - raio < 0 || y + raio >= alturaTela) {
            velocidadeY = -velocidadeY;
            y = Math.max(raio, Math.min(alturaTela - raio, y));
        }
    }

    public void mudarDirecao() {
        inicializarDirecao();
    }
}