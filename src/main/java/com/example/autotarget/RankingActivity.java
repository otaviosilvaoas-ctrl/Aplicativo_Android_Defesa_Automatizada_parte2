package com.example.autotarget;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    private ListView rankingList;
    private FirestoreRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        rankingList = findViewById(R.id.ranking_list);
        repository = new FirestoreRepository();

        carregarRanking();
    }

    private void carregarRanking() {
        repository.buscarRanking(new FirestoreRepository.RankingCallback() {
            @Override
            public void onSuccess(List<Partida> partidas) {
                runOnUiThread(() -> exibirRanking(partidas));
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(RankingActivity.this, "Erro ao carregar ranking: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void exibirRanking(List<Partida> partidas) {
        List<String> rankingDisplay = new ArrayList<>();
        for (int i = 0; i < partidas.size(); i++) {
            Partida p = partidas.get(i);
            String item = String.format("%dº - Pontos: %d | Alvos: %d", 
                    (i + 1), p.getPontuacao(), p.getAlvosAbatidos());
            rankingDisplay.add(item);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_list_item_1, rankingDisplay);
        rankingList.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repository != null) {
            repository.shutdown();
        }
    }
}
