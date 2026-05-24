package com.example.autotarget;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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
    private int tempoRestante = 60;
    private boolean cronometroIniciado = false;
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Corrigido erro de ambiguidade chamando explicitamente a classe pai
        super.setContentView(R.layout.activity_game_view);

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
                } else if (tempoRestante == 0) {
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

                // Inicia o cronômetro apenas na inserção do primeiro canhão
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
        statusText.setText("TEMPO ESGOTADO!");
        Toast.makeText(this, "Fim de jogo! Abates: " + jogo.getAbatesTotal(), Toast.LENGTH_LONG).show();
        
        // Bloqueia interações após o fim
        findViewById(R.id.add_cannon_button).setEnabled(false);
        
        // Retorna para a tela inicial após 3 segundos
        handler.postDelayed(() -> finish(), 3000);
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
    }
}