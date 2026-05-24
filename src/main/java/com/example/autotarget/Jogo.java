package com.example.autotarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classe principal que gerencia a lógica do jogo.
 * Implementa Runnable para ser controlado por uma Thread mestre.
 */
public class Jogo implements Runnable {
    private final List<Alvo> alvos;
    private final List<Canhao> canhoes;
    private boolean emExecucao;
    private int abatesTotal;
    private Thread threadPrincipal;

    private static final Object LOCK_ALVOS = new Object();
    private static final Object LOCK_CANHOES = new Object();
    private static final double DISTANCIA_MINIMA_CANHOES = 150.0;

    public Jogo() {
        this.alvos = new ArrayList<>();
        this.canhoes = new ArrayList<>();
        this.emExecucao = false;
        this.abatesTotal = 0;
    }

    @Override
    public void run() {
        while (emExecucao && !Thread.currentThread().isInterrupted()) {
            try {
                verificarColisoes();
                Thread.sleep(20); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Inicia o jogo, ativando as threads de processamento.
     * Corrigido para evitar double-start de threads.
     */
    public synchronized void iniciar() throws JogoException {
        if (emExecucao) {
            throw new JogoException("Jogo já está em execução");
        }

        // 1. Reativar e iniciar threads de objetos que já estavam na lista (ex: canhões do lobby ou alvos de restart)
        synchronized (LOCK_CANHOES) {
            synchronized (LOCK_ALVOS) {
                for (Canhao canhao : canhoes) {
                    canhao.setAtivo(true);
                    new Thread(canhao).start();
                }
                for (Alvo alvo : alvos) {
                    alvo.setAtivo(true);
                    new Thread(alvo).start();
                }

                // 2. Ativar o flag de execução enquanto segura os locks.
                // A partir deste ponto, adições via UI (adicionarCanhao) ou reposição (verificarColisoes)
                // dispararão suas threads automaticamente através de adicionarAlvo/Canhao.
                emExecucao = true;
            }
        }
        
        // 3. Criar os alvos iniciais da partida (serão iniciados via adicionarAlvo)
        criarAlvosIniciais();

        threadPrincipal = new Thread(this);
        threadPrincipal.start();
    }

    public synchronized void parar() {
        emExecucao = false;
        if (threadPrincipal != null) {
            threadPrincipal.interrupt();
        }
        synchronized (LOCK_ALVOS) {
            for (Alvo alvo : alvos) alvo.setAtivo(false);
        }
        synchronized (LOCK_CANHOES) {
            for (Canhao canhao : canhoes) canhao.setAtivo(false);
        }
    }

    private void criarAlvosIniciais() {
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            adicionarAlvo(new AlvoComum(random.nextDouble() * 800 + 100, 
                          random.nextDouble() * 1500 + 100, 40, 5));
        }
        for (int i = 0; i < 2; i++) {
            adicionarAlvo(new AlvoRapido(random.nextDouble() * 800 + 100, 
                          random.nextDouble() * 1500 + 100, 30, 8));
        }
    }

    private void adicionarAlvo(Alvo alvo) {
        synchronized (LOCK_ALVOS) {
            alvos.add(alvo);
            if (emExecucao) {
                alvo.setAtivo(true);
                new Thread(alvo).start();
            }
        }
    }

    public void adicionarCanhao(double x, double y) throws JogoException {
        synchronized (LOCK_CANHOES) {
            if (canhoes.size() >= 10) {
                throw new JogoException("Máximo de 10 canhões atingido");
            }

            for (Canhao existente : canhoes) {
                double dx = x - existente.getX();
                double dy = y - existente.getY();
                if (Math.sqrt(dx * dx + dy * dy) < DISTANCIA_MINIMA_CANHOES) {
                    x += 160; 
                }
            }

            Canhao novoCanhao = new Canhao(x, y, this);
            canhoes.add(novoCanhao);
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
                                break;
                            }
                        }
                    }
                }
                alvos.removeIf(a -> !a.isAtivo());
                
                if (alvos.size() < 5 && emExecucao) {
                    criarAlvosIniciais();
                }
            }
        }
        
        synchronized (LOCK_CANHOES) {
            for (Canhao c : canhoes) {
                c.limparProjeteis();
            }
        }
    }

    public List<Alvo> getAlvos() {
        synchronized (LOCK_ALVOS) { return new ArrayList<>(alvos); }
    }

    public List<Canhao> getCanhoes() {
        synchronized (LOCK_CANHOES) { return new ArrayList<>(canhoes); }
    }

    public int getAbatesTotal() { return abatesTotal; }
    public boolean isEmExecucao() { return emExecucao; }
}
