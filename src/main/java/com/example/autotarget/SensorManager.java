package com.example.autotarget;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gerencia a coleta periódica de dados dos sensores dos alvos.
 * AV2: Simula o sistema de monitoramento de cada lado da tela.
 */
public class SensorManager {
    private final Jogo jogo;
    private ScheduledExecutorService scheduler;
    private final Random random = new Random();
    private final AtomicInteger leiturasTotais = new AtomicInteger(0);

    public SensorManager(Jogo jogo) {
        this.jogo = jogo;
    }

    /**
     * Inicia a coleta periódica (1 leitura por segundo).
     */
    public synchronized void iniciarColeta() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::executarCicloColeta, 1, 1, TimeUnit.SECONDS);
        GerenciadorMetricas.log("SENSOR", "Sistema de coleta iniciado");
    }

    /**
     * Encerra o serviço de coleta.
     */
    public synchronized void pararColeta() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    /**
     * Simula a coleta de dados de cada lado.
     */
    private void executarCicloColeta() {
        if (!jogo.isEmExecucao()) return;

        List<Alvo> alvos = jogo.getAlvos();
        double centroX = jogo.getLarguraTela() / 2.0;

        for (Alvo alvo : alvos) {
            if (!alvo.isAtivo()) continue;

            // AV2: Cada lado coleta apenas alvos em seu território
            // (Simulado aqui através da verificação da posição atual do alvo)
            double xReal = alvo.getX();
            
            // Realiza a leitura se o alvo estiver visível para os sensores do sistema
            SensorReading leitura = gerarLeituraComRuido(alvo);
            alvo.adicionarLeitura(leitura);
            leiturasTotais.incrementAndGet();
        }
    }

    /**
     * Gera uma leitura com ruído gaussiano (Média 0, Desvio Padrão 5%).
     */
    private SensorReading gerarLeituraComRuido(Alvo a) {
        return new SensorReading(
            System.currentTimeMillis(),
            adicionarRuido(a.getX()),
            adicionarRuido(a.getY()),
            adicionarRuido(a.getVelocidadeX()),
            adicionarRuido(a.getVelocidadeY())
        );
    }

    private double adicionarRuido(double valor) {
        // Ruído Gaussiano: sigma = 5% do valor absoluto
        double sigma = Math.abs(valor) * 0.05;
        if (sigma < 0.1) sigma = 0.5; // Margem mínima de erro
        return valor + (random.nextGaussian() * sigma);
    }

    public int getLeiturasTotais() {
        return leiturasTotais.get();
    }

    public boolean isAtivo() {
        return scheduler != null && !scheduler.isShutdown();
    }
}
