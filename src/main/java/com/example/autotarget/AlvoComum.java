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

        double centro = larguraTela / 2.0;

        // Se larguraTela ainda não foi definida (0), usa comportamento padrão
        if (larguraTela > 0) {
            // Lógica de colisão com a linha central e bordas laterais
            if (x < centro) {
                // Alvo no lado ESQUERDO
                if (x - raio < 0) {
                    velocidadeX = Math.abs(velocidadeX);
                    x = raio;
                } else if (x + raio > centro) {
                    velocidadeX = -Math.abs(velocidadeX);
                    x = centro - raio;
                }
            } else {
                // Alvo no lado DIREITO
                if (x - raio < centro) {
                    velocidadeX = Math.abs(velocidadeX);
                    x = centro + raio;
                } else if (x + raio > larguraTela) {
                    velocidadeX = -Math.abs(velocidadeX);
                    x = larguraTela - raio;
                }
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
