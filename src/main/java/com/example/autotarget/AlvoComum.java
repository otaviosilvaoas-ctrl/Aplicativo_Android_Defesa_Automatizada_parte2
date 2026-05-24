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

        // O alvo agora se move livremente por toda a largura da tela
        if (larguraTela > 0) {
            if (x - raio < 0) {
                velocidadeX = Math.abs(velocidadeX);
                x = raio;
            } else if (x + raio > larguraTela) {
                velocidadeX = -Math.abs(velocidadeX);
                x = larguraTela - raio;
            }
        }

        // Verifica bordas verticais
        if (alturaTela > 0) {
            if (y - raio < 0) {
                velocidadeY = Math.abs(velocidadeY);
                y = raio;
            } else if (y + raio >= alturaTela) {
                velocidadeY = -Math.abs(velocidadeY);
                y = alturaTela - raio;
            }
        }
    }

    public void mudarDirecao() {
        inicializarDirecao();
    }
}
