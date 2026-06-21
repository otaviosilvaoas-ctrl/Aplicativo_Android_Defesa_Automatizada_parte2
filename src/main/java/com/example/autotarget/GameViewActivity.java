package com.example.autotarget;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;
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

        try {
            jogo.iniciar();
            emExecucao = true;
            iniciarThreadDeAtualizacao();
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

    private void finalizarJogo() {
        emExecucao = false;
        jogo.parar();

        // Persistência automática no Firestore
        Partida partida = new Partida(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                jogo.getAbatesTotal(),
                jogo.getAbatesTotal(),
                jogo.getCanhoes().size(),
                jogo.getEnergiaEsquerda() + jogo.getEnergiaDireita(),
                "anonimo"
        );
        FirestoreRepository repository = new FirestoreRepository();
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
            emExecucao = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
        if (jogo != null) jogo.parar();
        emExecucao = false;
    }
}
