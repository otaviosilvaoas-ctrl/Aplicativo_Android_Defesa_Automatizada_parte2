package com.example.autotarget;

import java.util.Random;

public class AlvoRapido extends Alvo {

    private Random random;
    private double velocidadeX;
    private double velocidadeY;
    private int contadorMudanca;
    private static final int INTERVALO_MUDANCA = 100; // Muda direção a cada 100 ciclos

    public AlvoRapido(double x, double y, double raio, double velocidade) {
        super(x, y, raio, velocidade * 1.5); // 50% mais rápido
        this.random = new Random();
        this.contadorMudanca = 0;
        inicializarDirecao();
    }

    /**
     * Inicializa a direção do movimento aleatoriamente.
     */
    private void inicializarDirecao() {
        double angulo = random.nextDouble() * 2 * Math.PI;
        this.velocidadeX = velocidade * Math.cos(angulo);
        this.velocidadeY = velocidade * Math.sin(angulo);
    }

    @Override
    public void mover() {
        // Incrementa contador de mudança
        contadorMudanca++;

        // Muda direção periodicamente
        if (contadorMudanca >= INTERVALO_MUDANCA) {
            inicializarDirecao();
            contadorMudanca = 0;
        }

        // Atualiza posição
        x += velocidadeX;
        y += velocidadeY;

        // Verifica colisão com as bordas e muda direção - Usando larguraTela e alturaTela
        if (x - raio <= 0 || x + raio >= larguraTela) {
            velocidadeX = -velocidadeX;
            x = Math.max(raio, Math.min(larguraTela - raio, x));
        }

        if (y - raio <= 0 || y + raio >= alturaTela) {
            velocidadeY = -velocidadeY;
            y = Math.max(raio, Math.min(alturaTela - raio, y));
        }
    }
}