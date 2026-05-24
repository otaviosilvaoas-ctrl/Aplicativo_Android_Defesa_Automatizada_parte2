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
        contadorMudanca++;
        if (contadorMudanca >= INTERVALO_MUDANCA) {
            inicializarDirecao();
            contadorMudanca = 0;
        }

        // Atualiza posição
        x += velocidadeX;
        y += velocidadeY;

        // O alvo agora se move livremente por toda a largura da tela (sem restrição central)
        if (larguraTela > 0) {
            if (x - raio < 0) {
                velocidadeX = Math.abs(velocidadeX);
                x = raio;
            } else if (x + raio > larguraTela) {
                velocidadeX = -Math.abs(velocidadeX);
                x = larguraTela - raio;
            }
        }

        // Verifica colisão com as bordas verticais
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
}
