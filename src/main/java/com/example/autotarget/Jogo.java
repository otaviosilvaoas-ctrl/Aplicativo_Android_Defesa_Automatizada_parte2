package com.example.autotarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
    
    // Recursos AV2
    private final AtomicInteger energiaEsquerda = new AtomicInteger(100);
    private final AtomicInteger energiaDireita = new AtomicInteger(100);
    public static final int LIMITE_CANHOES_LADO = 5;
    
    // Sensores e Reconciliação AV2
    private final SensorManager sensorManager;
    private ScheduledExecutorService reconciliationScheduler;
    private double erroReconciliacaoAntes = 0;
    private double erroReconciliacaoDepois = 0;
    private int leiturasReconciliacaoUsadas = 0;

    // Decisão Automática AV2
    private double utilidadeEsq = 0;
    private double utilidadeDir = 0;
    private String últimaDecisaoEsq = "NENHUMA";
    private String últimaDecisaoDir = "NENHUMA";
    private static final double LIMIAR_ADICAO = 0.4; 
    private static final double LIMIAR_REMOCAO = 0.1;
    private static final int MAX_CANHOES_POR_LADO = 10;
    private static final int MIN_CANHOES_POR_LADO = 1;
    
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
        this.sensorManager = new SensorManager(this);
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
        
        sensorManager.iniciarColeta();
        iniciarReconciliacao();
        
        criarAlvosIniciais();
        threadPrincipal = new Thread(this);
        threadPrincipal.start();
        adicionarLog("Jogo Iniciado");
    }

    public synchronized void parar() {
        emExecucao = false;
        sensorManager.pararColeta();
        pararReconciliacao();
        
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

    private void iniciarReconciliacao() {
        reconciliationScheduler = Executors.newSingleThreadScheduledExecutor();
        // Ciclo de 10 segundos para Reconciliação + Decisão IA
        reconciliationScheduler.scheduleWithFixedDelay(this::processarReconciliacaoEDecisao, 10, 10, TimeUnit.SECONDS);
    }

    private void pararReconciliacao() {
        if (reconciliationScheduler != null) {
            reconciliationScheduler.shutdownNow();
        }
    }

    private void processarReconciliacaoEDecisao() {
        if (!emExecucao) return;
        GerenciadorMetricas.log("SISTEMA", "Ciclo de Otimização e Decisão IA iniciado");
        
        otimizarEDecidirLado(true);  // Esquerda
        otimizarEDecidirLado(false); // Direita
    }

    private void otimizarEDecidirLado(boolean esquerda) {
        double centroX = larguraTela / 2.0;
        List<Alvo> alvosLado = new ArrayList<>();
        synchronized (LOCK_ALVOS) {
            for (Alvo a : alvos) {
                if (a.isAtivo() && (esquerda ? a.getX() < centroX : a.getX() >= centroX)) {
                    alvosLado.add(a);
                }
            }
        }

        List<Canhao> canhoesLado = new ArrayList<>();
        synchronized (LOCK_CANHOES) {
            for (Canhao c : canhoes) {
                if (c.isAtivo() && (esquerda ? c.getX() < centroX : c.getX() >= centroX)) {
                    canhoesLado.add(c);
                }
            }
        }

        double[] yHat = null;
        int nCanhoes = canhoesLado.size();
        int nAlvos = alvosLado.size();

        // 1. RECONCILIAÇÃO (Se houver elementos suficientes)
        if (nCanhoes > 0 && nAlvos > 0) {
            int nVar = nCanhoes * nAlvos;
            double[] y = new double[nVar];
            double[][] V = new double[nVar][nVar];
            int idx = 0;

            for (Canhao c : canhoesLado) {
                for (Alvo a : alvosLado) {
                    List<SensorReading> leituras = a.getHistoricoLeituras();
                    if (leituras.size() < 2) {
                        y[idx] = Math.hypot(c.getX() - a.getX(), c.getY() - a.getY());
                        V[idx][idx] = 1.0;
                    } else {
                        double somaDist = 0;
                        for (SensorReading r : leituras) somaDist += Math.hypot(c.getX() - r.x, c.getY() - r.y);
                        double mediaDist = somaDist / leituras.size();
                        double somaVar = 0;
                        for (SensorReading r : leituras) somaVar += Math.pow(Math.hypot(c.getX() - r.x, c.getY() - r.y) - mediaDist, 2);
                        y[idx] = mediaDist;
                        V[idx][idx] = Math.max(0.1, somaVar / (leituras.size() - 1));
                    }
                    idx++;
                }
            }

            double[][] A = new double[nCanhoes][nVar];
            for (int i = 0; i < nCanhoes; i++) {
                for (int j = 0; j < nAlvos; j++) {
                    if (y[i * nAlvos + j] < 500.0) A[i][i * nAlvos + j] = 1.0;
                }
            }

            yHat = DataReconciliation.reconcile(y, V, A);

            // Atualiza métricas de erro para o HUD
            if (esquerda) {
                double erro = 0;
                double[] Ay = MatrixMath.multiply(A, y);
                for (double val : Ay) erro += Math.abs(val);
                erroReconciliacaoAntes = erro / Math.max(1, A.length);
                
                erro = 0;
                double[] AyHat = MatrixMath.multiply(A, yHat);
                for (double val : AyHat) erro += Math.abs(val);
                erroReconciliacaoDepois = erro / Math.max(1, A.length);
                leiturasReconciliacaoUsadas = nVar;
            }

            // Otimização de Posicionamento (Movimentação Gradual)
            idx = 0;
            for (Canhao c : canhoesLado) {
                double somaX = 0, somaY = 0, pesoTotal = 0;
                for (Alvo a : alvosLado) {
                    double peso = 1.0 / Math.max(10.0, yHat[idx]);
                    somaX += a.getX() * peso;
                    somaY += a.getY() * peso;
                    pesoTotal += peso;
                    idx++;
                }
                if (pesoTotal > 0) {
                    double targetX = somaX / pesoTotal;
                    double targetY = somaY / pesoTotal;
                    // Respeita os limites do lado
                    if (esquerda) targetX = Math.min(targetX, centroX - 100);
                    else targetX = Math.max(targetX, centroX + 100);
                    c.setPosicaoObjetivo(targetX, targetY);
                }
            }
        }

        // 2. TOMADA DE DECISÃO (Função de Utilidade)
        double utilidadeAtual = calcularUtilidade(nCanhoes, nAlvos, esquerda, yHat);
        if (esquerda) utilidadeEsq = utilidadeAtual; else utilidadeDir = utilidadeAtual;

        double utilSeAdicionar = calcularUtilidade(nCanhoes + 1, nAlvos, esquerda, null);
        double utilSeRemover = nCanhoes > MIN_CANHOES_POR_LADO ? calcularUtilidade(nCanhoes - 1, nAlvos, esquerda, null) : -1e10;

        if (utilSeAdicionar > utilidadeAtual + LIMIAR_ADICAO && nCanhoes < MAX_CANHOES_POR_LADO) {
            executarAdicaoAutomatica(esquerda);
            if (esquerda) últimaDecisaoEsq = "ADICIONAR"; else últimaDecisaoDir = "ADICIONAR";
        } else if (nCanhoes > MIN_CANHOES_POR_LADO && (utilSeRemover > utilidadeAtual - LIMIAR_REMOCAO || getEnergiaLado(esquerda) < 10)) {
            // Remove se a utilidade melhorar ou se a energia for crítica
            if (utilSeRemover > utilidadeAtual || getEnergiaLado(esquerda) < 10) {
                executarRemocaoAutomatica(esquerda);
                if (esquerda) últimaDecisaoEsq = "REMOVER"; else últimaDecisaoDir = "REMOVER";
            } else {
                if (esquerda) últimaDecisaoEsq = "MANTER"; else últimaDecisaoDir = "MANTER";
            }
        } else {
            if (esquerda) últimaDecisaoEsq = "MANTER"; else últimaDecisaoDir = "MANTER";
        }
    }

    /**
     * Função de Utilidade: estima o benefício líquido de manter N canhões.
     * Considera abates esperados, penalidades, energia e proximidade (yHat).
     */
    private double calcularUtilidade(int nC, int nA, boolean esquerda, double[] yHat) {
        if (nC <= 0) return -1.0;
        
        // Penalidade AV2: +20% delay por canhão acima de 5
        double penalidade = nC > LIMITE_CANHOES_LADO ? (nC - LIMITE_CANHOES_LADO) * 0.2 : 0;
        double fatorEficienciaRecarga = 1.0 / (1.0 + penalidade);
        
        int energia = getEnergiaLado(esquerda);
        double fatorEnergia = energia / 100.0;
        
        // Fator de Proximidade baseado na Reconciliação
        double fatorProximidade = 0.5; // Valor base se não houver yHat
        if (yHat != null && yHat.length > 0) {
            double somaInvDist = 0;
            for (double d : yHat) somaInvDist += 200.0 / Math.max(20.0, d);
            fatorProximidade = somaInvDist / (nC * Math.max(1, nA));
        }

        // Estimativa de abates (Ganho)
        double ganhoAbates = (nC * nA * 0.08) * fatorEficienciaRecarga * (0.3 + 0.7 * fatorProximidade);
        
        // Custos (Penalidades e Recursos)
        double custoOperacional = nC * 0.15; // Custo fixo por canhão (manutenção/threads)
        double custoEnergia = (1.0 - fatorEnergia) * nC * 0.4; // Piora conforme energia acaba
        
        return ganhoAbates - (custoOperacional + custoEnergia);
    }

    private int getEnergiaLado(boolean esquerda) {
        return esquerda ? energiaEsquerda.get() : energiaDireita.get();
    }

    private void executarAdicaoAutomatica(boolean esquerda) {
        Random r = new Random();
        double x = esquerda ? (larguraTela * 0.1) + r.nextDouble() * (larguraTela * 0.3) 
                            : (larguraTela * 0.6) + r.nextDouble() * (larguraTela * 0.3);
        double y = (alturaTela * 0.2) + r.nextDouble() * (alturaTela * 0.6);
        try {
            adicionarCanhao(x, y);
        } catch (JogoException e) { /* limite atingido */ }
    }

    private void executarRemocaoAutomatica(boolean esquerda) {
        synchronized (LOCK_CANHOES) {
            double centroX = larguraTela / 2.0;
            for (int i = 0; i < canhoes.size(); i++) {
                Canhao c = canhoes.get(i);
                boolean isEsq = c.getX() < centroX;
                if (isEsq == esquerda) {
                    c.setAtivo(false);
                    canhoes.remove(i);
                    adicionarLog("IA: Removido Canhão " + c.getId());
                    break;
                }
            }
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
            if (Math.abs(x - centro) < margemSeguranca) x = (x < centro) ? centro - margemSeguranca : centro + margemSeguranca;
            for (Canhao existente : canhoes) {
                if (Math.hypot(x - existente.getX(), y - existente.getY()) < DISTANCIA_MINIMA_CANHOES) {
                    x += 160;
                    if (Math.abs(x - centro) < margemSeguranca) x = centro + margemSeguranca;
                }
            }
            Canhao novoCanhao = new Canhao(x, y, this, proximoIdCanhao++);
            canhoes.add(novoCanhao);
            adicionarLog("IA: Adicionado Canhão " + novoCanhao.getId());
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
                                if (alvo.getX() < larguraTela / 2.0) abatesEsquerda++; else abatesDireita++;
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
        synchronized (LOCK_CANHOES) { for (Canhao c : canhoes) c.limparProjeteis(); }
    }

    public SensorManager getSensorManager() { return sensorManager; }
    public double getErroRecAntes() { return erroReconciliacaoAntes; }
    public double getErroRecDepois() { return erroReconciliacaoDepois; }
    public int getLeiturasRecUsadas() { return leiturasReconciliacaoUsadas; }
    public double getUtilidadeEsq() { return utilidadeEsq; }
    public double getUtilidadeDir() { return utilidadeDir; }
    public String getÚltimaDecisaoEsq() { return últimaDecisaoEsq; }
    public String getÚltimaDecisaoDir() { return últimaDecisaoDir; }

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
            for (Canhao c : canhoes) { if ((c.getX() < centro) == esquerda) count++; }
        }
        return count;
    }
    public double getPenalidadeLado(boolean esquerda) {
        int n = getQtdCanhoesLado(esquerda);
        return n <= LIMITE_CANHOES_LADO ? 0.0 : (n - LIMITE_CANHOES_LADO) * 0.2;
    }
    public List<Alvo> getAlvos() { synchronized (LOCK_ALVOS) { return new ArrayList<>(alvos); } }
    public List<Canhao> getCanhoes() { synchronized (LOCK_CANHOES) { return new ArrayList<>(canhoes); } }
    public int getAbatesTotal() { return abatesTotal; }
    public int getAbatesEsquerda() { return abatesEsquerda; }
    public int getAbatesDireita() { return abatesDireita; }
    public boolean isEmExecucao() { return emExecucao; }
    public int getLarguraTela() { return larguraTela; }
    public int getAlturaTela() { return alturaTela; }
}
