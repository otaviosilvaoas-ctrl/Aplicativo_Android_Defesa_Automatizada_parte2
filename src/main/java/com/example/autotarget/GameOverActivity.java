package com.example.autotarget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        int abatesEsquerda = getIntent().getIntExtra("ABATES_ESQUERDA", 0);
        int abatesDireita = getIntent().getIntExtra("ABATES_DIREITA", 0);
        int totalCanhoes = getIntent().getIntExtra("TOTAL_CANHOES", 0);
        int totalAlvos = getIntent().getIntExtra("TOTAL_ALVOS", 0);
        int tempo = getIntent().getIntExtra("TEMPO_TOTAL", 60);

        TextView textVencedor = findViewById(R.id.text_vencedor);
        TextView textPlacar = findViewById(R.id.text_placar);
        TextView textDetEsq = findViewById(R.id.text_detalhes_esquerda);
        TextView textDetDir = findViewById(R.id.text_detalhes_direita);
        TextView textCanhoes = findViewById(R.id.text_total_canhoes);
        TextView textAlvos = findViewById(R.id.text_total_alvos);
        TextView textTempo = findViewById(R.id.text_tempo_total);

        // Lógica do Vencedor
        if (abatesEsquerda > abatesDireita) {
            textVencedor.setText("VENCEDOR: LADO ESQUERDO");
            textVencedor.setTextColor(0xFF2E7D32); // Verde
        } else if (abatesDireita > abatesEsquerda) {
            textVencedor.setText("VENCEDOR: LADO DIREITO");
            textVencedor.setTextColor(0xFF1976D2); // Azul
        } else {
            textVencedor.setText("EMPATE!");
            textVencedor.setTextColor(0xFFFBC02D); // Amarelo/Laranja
        }

        textPlacar.setText(String.format("Placar: %d x %d", abatesEsquerda, abatesDireita));
        textDetEsq.setText("Abates Lado Esquerdo: " + abatesEsquerda);
        textDetDir.setText("Abates Lado Direito: " + abatesDireita);
        textCanhoes.setText("Total de Canhões: " + totalCanhoes);
        textAlvos.setText("Alvos Destruídos: " + totalAlvos);
        textTempo.setText("Tempo Total: " + tempo + "s");

        Button btnReiniciar = findViewById(R.id.btn_reiniciar);
        btnReiniciar.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        Button btnSair = findViewById(R.id.btn_sair);
        btnSair.setOnClickListener(v -> {
            finishAffinity(); // Fecha todas as activities
        });
    }
}
