package com.example.autotarget;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Locale;

/**
 * Exibe o resultado final da partida e a análise de escalonabilidade AV2.
 */
public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // 1. Recuperação de Dados da Partida
        Intent intent = getIntent();
        int abatesEsquerda = intent.getIntExtra("ABATES_ESQUERDA", 0);
        int abatesDireita = intent.getIntExtra("ABATES_DIREITA", 0);
        int totalCanhoes = intent.getIntExtra("TOTAL_CANHOES", 0);
        int totalAlvos = intent.getIntExtra("TOTAL_ALVOS", 0);
        int tempoTotal = intent.getIntExtra("TEMPO_TOTAL", 60);
        int energiaEsq = intent.getIntExtra("ENERGIA_ESQ", 0);
        int energiaDir = intent.getIntExtra("ENERGIA_DIR", 0);

        // 2. Preenchimento do Cabeçalho e Placar
        TextView textVencedor = findViewById(R.id.text_vencedor);
        TextView textPlacar = findViewById(R.id.text_placar);
        
        if (abatesEsquerda > abatesDireita) {
            textVencedor.setText("VENCEDOR: LADO ESQUERDO");
            textVencedor.setTextColor(0xFF2E7D32); // Verde
        } else if (abatesDireita > abatesEsquerda) {
            textVencedor.setText("VENCEDOR: LADO DIREITO");
            textVencedor.setTextColor(0xFF1976D2); // Azul
        } else {
            textVencedor.setText("EMPATE TÉCNICO!");
            textVencedor.setTextColor(0xFFFBC02D); // Amarelo
        }

        textPlacar.setText(String.format(Locale.US, "%d x %d", abatesEsquerda, abatesDireita));

        // 3. Detalhes Estatísticos
        ((TextView)findViewById(R.id.text_detalhes_esquerda)).setText("Abates Esq: " + abatesEsquerda);
        ((TextView)findViewById(R.id.text_detalhes_direita)).setText("Abates Dir: " + abatesDireita);
        ((TextView)findViewById(R.id.text_total_canhoes)).setText("Canhões: " + totalCanhoes);
        ((TextView)findViewById(R.id.text_total_alvos)).setText("Alvos Totais: " + totalAlvos);
        ((TextView)findViewById(R.id.text_tempo_total)).setText("Duração: " + tempoTotal + "s");
        ((TextView)findViewById(R.id.text_energia_final)).setText(String.format(Locale.US, "Energia Final: %d%% | %d%%", energiaEsq, energiaDir));

        // 4. TABELA DE ESCALONABILIDADE (AV2)
        TableLayout table = findViewById(R.id.table_rt_analysis);
        List<TaskMetrics> allTasks = RealTimeScheduler.getAllMetrics();

        for (TaskMetrics m : allTasks) {
            TableRow row = new TableRow(this);
            row.setPadding(0, 2, 0, 2);
            
            row.addView(createTableCell(m.id, false));
            row.addView(createTableCell(m.descricao, false));
            row.addView(createTableCell(String.valueOf(m.prioridade), false));
            row.addView(createTableCell(String.valueOf(m.period), false));
            row.addView(createTableCell(String.format(Locale.US, "%.1f", m.getAverageCi()), false));
            row.addView(createTableCell(String.valueOf(m.deadline), false));
            row.addView(createTableCell(String.format(Locale.US, "%.1f", m.getAverageJitter()), false));
            row.addView(createTableCell(String.valueOf(m.getRi()), false));
            
            table.addView(row);
        }

        // 5. MÉTRICAS TÉCNICAS E UTILIZAÇÃO
        double util = RealTimeScheduler.getTotalUtilization();
        double throughput = (double) totalAlvos / Math.max(1, tempoTotal);
        
        TextView textResumo = findViewById(R.id.text_resumo_rt);
        String infoTecnica = String.format(Locale.US, 
                "ANÁLISE DO SISTEMA:\n" +
                "• Throughput: %.2f alvos/seg\n" +
                "• Utilização do Processador (U): %.3f\n" +
                "• Deadlines Perdidos: %d\n" +
                "• Escalonabilidade RM: %s",
                throughput, util, RealTimeScheduler.getTotalDeadlinesMissed(),
                util <= 1.0 ? "ESTÁVEL (Escalonável)" : "SOBRECARREGADO");
        textResumo.setText(infoTecnica);

        // 6. Botões de Ação
        findViewById(R.id.btn_reiniciar).setOnClickListener(v -> {
            Intent restart = new Intent(this, GameViewActivity.class);
            restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(restart);
            finish();
        });

        findViewById(R.id.btn_sair).setOnClickListener(v -> finishAffinity());
    }

    private TextView createTableCell(String text, boolean header) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(10, 10, 10, 10);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(11);
        tv.setTextColor(Color.BLACK);
        if (header) {
            tv.setTypeface(null, Typeface.BOLD);
            tv.setBackgroundColor(0xFFDDDDDD);
        }
        return tv;
    }
}
