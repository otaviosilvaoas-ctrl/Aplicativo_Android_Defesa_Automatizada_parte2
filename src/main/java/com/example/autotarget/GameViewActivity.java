package com.example.autotarget;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.autotarget.Partida;
import com.example.autotarget.FirestoreRepository;

/**
 * Activity principal do jogo AutoTarget.
 * Gerencia o cronômetro de 60s e a execução do jogo.
 */
public class GameViewActivity extends AppCompatActivity {

    private JogoView jogoView;
    private Jogo jogo;
    private Handler handler;
    private Thread gameThread;
    private boolean emExecucao;
    private static final int INTERVALO_ATUALIZACAO = 50; 

    private TextView timerText;
    private TextView statusText;
    private int tempoPartida = 60;
    private int tempoRestante = tempoPartida;
    private boolean cronometroIniciado = false;
    private Runnable timerRunnable;

    // AV3 Letra D: Atributos para o Sistema Ciberfísico
    private ScheduledExecutorService telemetriaExecutor;
    private SensorTemperatura sensorTemperatura;
    private FirestoreRepository repository;
    
    // AV3 Letra E: Gerenciador de Autenticação
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_view);

        jogoView = findViewById(R.id.jogo_view);
        Button addCannonButton = findViewById(R.id.add_cannon_button);
        statusText = findViewById(R.id.status_text);
        timerText = findViewById(R.id.timer_text);

        jogo = new Jogo();
        jogoView.setJogo(jogo);
        handler = new Handler(Looper.getMainLooper());
        repository = new FirestoreRepository();
        sensorTemperatura = new SensorTemperatura();
        authManager = new AuthManager();

        try {
            jogo.iniciar();
            emExecucao = true;
            iniciarThreadDeAtualizacao();
            iniciarTelemetria(); // AV3 Letra D: Inicia a coleta de telemetria
        } catch (JogoException e) {
            statusText.setText("Erro: " + e.getMessage());
        }

        // Lógica do Cronômetro
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (tempoRestante > 0 && emExecucao) {
                    tempoRestante--;
                    timerText.setText(tempoRestante + "s");
                    handler.postDelayed(this, 1000);
                } else if (tempoRestante == 0 && emExecucao) {
                    finalizarJogo();
                }
            }
        };

        addCannonButton.setOnClickListener(v -> {
            try {
                float larguraView = jogoView.getWidth();
                float alturaView = jogoView.getHeight();
                if (larguraView == 0) larguraView = getResources().getDisplayMetrics().widthPixels;
                if (alturaView == 0) alturaView = getResources().getDisplayMetrics().heightPixels;

                float xAleatorio = 100 + (float)(Math.random() * (larguraView - 200));
                float yAleatorio = 100 + (float)(Math.random() * (alturaView - 200));

                jogo.adicionarCanhao(xAleatorio, yAleatorio);
                statusText.setText("Jogo em Execução");

                if (!cronometroIniciado) {
                    cronometroIniciado = true;
                    handler.postDelayed(timerRunnable, 1000);
                }
            } catch (JogoException e) {
                statusText.setText("Erro: " + e.getMessage());
            }
        });
    }

    /**
     * AV3 Letra D: Implementação do Sistema Ciberfísico com Controle de Feedback Térmico.
     */
    private void iniciarTelemetria() {
        telemetriaExecutor = Executors.newSingleThreadScheduledExecutor();
        telemetriaExecutor.scheduleWithFixedDelay(() -> {
            if (emExecucao) {
                // 1. Leitura do Sensor
                float temp = sensorTemperatura.lerTemperatura();
                Telemetria t = new Telemetria(temp, System.currentTimeMillis());

                // 2. Persistência no Firestore
                repository.salvarTelemetria(t);

                // 3. Lógica de Controle Feedback
                if (temp > 40.0f) {
                    jogo.setFatorTermico(2.0); // Reduz taxa de disparo (dobra o intervalo)
                    Log.d("CPS_Control", "AV3 D - Alerta Térmico: " + temp + "°C. Modo de proteção ativado.");
                } else {
                    jogo.setFatorTermico(1.0); // Restaura taxa normal
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void finalizarJogo() {
        emExecucao = false;
        jogo.parar();
        pararTelemetria(); // AV3 Letra D: Para a coleta ao fim do jogo

        // AV3 Letra E: Obtendo o UID real do usuário autenticado para a persistência
        String usuarioId = authManager.getUsuarioAtualUid();

        // Persistência automática no Firestore (AV3 Letra B/C/E)
        Partida partida = new Partida(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                jogo.getAbatesTotal(),
                jogo.getAbatesTotal(),
                jogo.getCanhoes().size(),
                jogo.getEnergiaEsquerda() + jogo.getEnergiaDireita(),
                usuarioId
        );
        repository.salvarPartida(partida);
        
        // Prepara dados para a tela de Game Over
        Intent intent = new Intent(this, GameOverActivity.class);
        intent.putExtra("ABATES_ESQUERDA", jogo.getAbatesEsquerda());
        intent.putExtra("ABATES_DIREITA", jogo.getAbatesDireita());
        intent.putExtra("TOTAL_CANHOES", jogo.getCanhoes().size());
        intent.putExtra("TOTAL_ALVOS", jogo.getAbatesTotal());
        intent.putExtra("TEMPO_TOTAL", tempoPartida);
        intent.putExtra("ENERGIA_ESQ", jogo.getEnergiaEsquerda());
        intent.putExtra("ENERGIA_DIR", jogo.getEnergiaDireita());
        
        startActivity(intent);
        finish();
    }

    private void pararTelemetria() {
        if (telemetriaExecutor != null) {
            telemetriaExecutor.shutdownNow();
        }
    }

    private void iniciarThreadDeAtualizacao() {
        gameThread = new Thread(() -> {
            while (emExecucao) {
                try {
                    jogo.verificarColisoes();
                    handler.post(() -> jogoView.invalidate());
                    Thread.sleep(INTERVALO_ATUALIZACAO);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        gameThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (emExecucao) {
            jogo.parar();
            pararTelemetria();
            emExecucao = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
        if (jogo != null) jogo.parar();
        pararTelemetria();
        if (repository != null) repository.shutdown();
        emExecucao = false;
    }
}
