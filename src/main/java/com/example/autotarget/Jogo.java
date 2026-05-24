package com.example.autotarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Classe principal que gerencia a lógica do jogo.
 * Implementa Runnable para ser controlado por uma Thread mestre.
 */
public class Jogo implements Runnable {
    private final List<Alvo> alvos;
    private final List<Canhao> canhoes;
    private final List<String> logsTela;
    private boolean emExecucao;
    private int abatesTotal;
    private int abatesEsquerda;
    private int abatesDireita;
    private int proximoIdCanhao = 1;
    private Thread threadPrincipal;
    
    private int larguraTela;
    private int alturaTela;
    
    // Recursos AV2: Energia por lado
    private final AtomicInteger energiaEsquerda = new AtomicInteger(100);
    private final AtomicInteger energiaDireita = new AtomicInteger(100);
    public static final int LIMITE_CANHOES_LADO = 5;
    
    private ExecutorService executorProjeteis;

    private static final Object LOCK_ALVOS = new Object();
    private static final Object LOCK_CANHOES = new Object();
    private static final double DISTANCIA_MINIMA_CANHOES = 150.0;
    private static final int POOL_PROJETEIS = 30;

    public Jogo() {
        this.alvos = new ArrayList<>();
        this.canhoes = new ArrayList<>();
        this.logsTela = Collections.synchronizedList(new ArrayList<>());
        this.emExecucao = false;
        this.abatesTotal = 0;
        this.abatesEsquerda = 0;
        this.abatesDireita = 0;
    }

    @Override
    public void run() {
        while (emExecucao && !Thread.currentThread().isInterrupted()) {
            long inicio = System.currentTimeMillis();
            try {
                verificarColisoes();
                
                if (System.currentTimeMillis() % 1000 < 25) {
                    int numProjeteis = 0;
                    synchronized (LOCK_CANHOES) {
                        for (Canhao c : canhoes) numProjeteis += c.getProjeteis().size();
                    }
                    GerenciadorMetricas.logEstado(alvos.size(), canhoes.size(), numProjeteis);
                }

                long fim = System.currentTimeMillis();
                GerenciadorMetricas.registrarTempoLoop(fim - inicio);

                Thread.sleep(20); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void adicionarLog(String msg) {
        logsTela.add(msg);
        if (logsTela.size() > 5) {
            logsTela.remove(0);
        }
    }

    public List<String> getLogsTela() {
        return new ArrayList<>(logsTela);
    }

    public void setDimensoes(int largura, int altura) {
        this.larguraTela = largura;
        this.alturaTela = altura;
        synchronized (LOCK_ALVOS) {
            for (Alvo alvo : alvos) alvo.setLimitesTela(largura, altura);
        }
    }

    public synchronized void iniciar() throws JogoException {
        if (emExecucao) throw new JogoException("Jogo já está em execução");
        executorProjeteis = Executors.newFixedThreadPool(POOL_PROJETEIS);
        energiaEsquerda.set(100);
        energiaDireita.set(100);
        synchronized (LOCK_CANHOES) {
            synchronized (LOCK_ALVOS) {
                for (Canhao canhao : canhoes) {
                    canhao.setAtivo(true);
                    new Thread(canhao).start();
                }
                for (Alvo alvo : alvos) {
                    alvo.setAtivo(true);
                    alvo.setLimitesTela(larguraTela, alturaTela);
                    new Thread(alvo).start();
                }
                emExecucao = true;
            }
        }
        criarAlvosIniciais();
        threadPrincipal = new Thread(this);
        threadPrincipal.start();
        adicionarLog("Jogo Iniciado");
    }

    public synchronized void parar() {
        emExecucao = false;
        if (executorProjeteis != null) {
            executorProjeteis.shutdownNow();
        }
        if (threadPrincipal != null) threadPrincipal.interrupt();
        synchronized (LOCK_ALVOS) {
            for (Alvo alvo : alvos) alvo.setAtivo(false);
        }
        synchronized (LOCK_CANHOES) {
            for (Canhao canhao : canhoes) canhao.setAtivo(false);
        }
    }

    public void dispararProjetil(Projetil p) {
        if (emExecucao && executorProjeteis != null && !executorProjeteis.isShutdown()) {
            p.setLimitesTela(larguraTela, alturaTela);
            executorProjeteis.submit(p);
        }
    }

    private void criarAlvosIniciais() {
        Random random = new Random();
        double w = larguraTela > 0 ? larguraTela : 1000;
        double h = alturaTela > 0 ? alturaTela : 1000;
        for (int i = 0; i < 3; i++) {
            adicionarAlvo(new AlvoComum(random.nextDouble() * (w * 0.8) + (w * 0.1), 
                          random.nextDouble() * (h * 0.8) + (h * 0.1), 40, 5));
        }
        for (int i = 0; i < 2; i++) {
            adicionarAlvo(new AlvoRapido(random.nextDouble() * (w * 0.8) + (w * 0.1), 
                          random.nextDouble() * (h * 0.8) + (h * 0.1), 30, 8));
        }
    }

    private void adicionarAlvo(Alvo alvo) {
        synchronized (LOCK_ALVOS) {
            alvo.setLimitesTela(larguraTela, alturaTela);
            alvos.add(alvo);
            if (emExecucao) {
                alvo.setAtivo(true);
                new Thread(alvo).start();
            }
        }
    }

    public void adicionarCanhao(double x, double y) throws JogoException {
        synchronized (LOCK_CANHOES) {
            if (canhoes.size() >= 20) throw new JogoException("Máximo de 20 canhões atingido");
            
            double centro = larguraTela / 2.0;
            double margemSeguranca = 80.0;
            
            if (Math.abs(x - centro) < margemSeguranca) {
                x = (x < centro) ? centro - margemSeguranca : centro + margemSeguranca;
            }

            for (Canhao existente : canhoes) {
                if (Math.hypot(x - existente.getX(), y - existente.getY()) < DISTANCIA_MINIMA_CANHOES) {
                    x += 160;
                    if (Math.abs(x - centro) < margemSeguranca) x = centro + margemSeguranca;
                }
            }

            Canhao novoCanhao = new Canhao(x, y, this, proximoIdCanhao++);
            canhoes.add(novoCanhao);
            adicionarLog("Canhão " + novoCanhao.getId() + " adicionado");
            if (emExecucao) {
                novoCanhao.setAtivo(true);
                new Thread(novoCanhao).start();
            }
        }
    }

    public void verificarColisoes() {
        synchronized (LOCK_ALVOS) {
            synchronized (LOCK_CANHOES) {
                for (int i = alvos.size() - 1; i >= 0; i--) {
                    Alvo alvo = alvos.get(i);
                    if (!alvo.isAtivo()) continue;
                    for (Canhao canhao : canhoes) {
                        for (Projetil projetil : canhao.getProjeteis()) {
                            if (projetil.isAtivo() && alvo.verificarColisao(projetil)) {
                                alvo.setAtivo(false);
                                projetil.setAtivo(false);
                                abatesTotal++;
                                
                                if (alvo.getX() < larguraTela / 2.0) abatesEsquerda++;
                                else abatesDireita++;
                                
                                GerenciadorMetricas.registrarColisao();
                                adicionarLog("Alvo destruído!");
                                break;
                            }
                        }
                    }
                }
                alvos.removeIf(a -> !a.isAtivo());
                if (alvos.size() < 5 && emExecucao) criarAlvosIniciais();
            }
        }
        synchronized (LOCK_CANHOES) {
            for (Canhao c : canhoes) c.limparProjeteis();
        }
    }

    // Lógica de Recursos AV2
    public int getEnergiaEsquerda() { return energiaEsquerda.get(); }
    public int getEnergiaDireita() { return energiaDireita.get(); }
    
    public boolean consumirEnergia(double posX) {
        AtomicInteger energiaLado = (posX < larguraTela / 2.0) ? energiaEsquerda : energiaDireita;
        while (true) {
            int atual = energiaLado.get();
            if (atual <= 0) return false;
            if (energiaLado.compareAndSet(atual, atual - 1)) return true;
        }
    }

    public int getQtdCanhoesLado(boolean esquerda) {
        int count = 0;
        double centro = larguraTela / 2.0;
        synchronized (LOCK_CANHOES) {
            for (Canhao c : canhoes) {
                boolean e = c.getX() < centro;
                if (e == esquerda) count++;
            }
        }
        return count;
    }

    public double getPenalidadeLado(boolean esquerda) {
        int n = getQtdCanhoesLado(esquerda);
        if (n <= LIMITE_CANHOES_LADO) return 0.0;
        return (n - LIMITE_CANHOES_LADO) * 0.2;
    }

    public List<Alvo> getAlvos() {
        synchronized (LOCK_ALVOS) { return new ArrayList<>(alvos); }
    }

    public List<Canhao> getCanhoes() {
        synchronized (LOCK_CANHOES) { return new ArrayList<>(canhoes); }
    }

    public int getAbatesTotal() { return abatesTotal; }
    public int getAbatesEsquerda() { return abatesEsquerda; }
    public int getAbatesDireita() { return abatesDireita; }
    public boolean isEmExecucao() { return emExecucao; }
    public int getLarguraTela() { return larguraTela; }
    public int getAlturaTela() { return alturaTela; }
}
