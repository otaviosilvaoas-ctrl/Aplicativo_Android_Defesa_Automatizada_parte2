package com.example.autotarget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        authManager = new AuthManager();
        
        // AV3 Letra E: Bloqueio de acesso se não houver usuário autenticado
        if (!authManager.isUsuarioLogado()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        TextView titleView = findViewById(R.id.title);
        titleView.setText(R.string.app_name);

        TextView subtitleView = findViewById(R.id.subtitle);
        subtitleView.setText(R.string.Subtitle);

        // Botão para iniciar o jogo
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, GameViewActivity.class);
            startActivity(intent);
        });

        // Botão para abrir o Ranking (AV3 Letra A)
        Button rankingButton = findViewById(R.id.rankingButton);
        rankingButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, RankingActivity.class);
            startActivity(intent);
        });
        
        // Botão Logout (Opcional, mas útil para testes de segurança)
        Button logoutButton = new Button(this);
        logoutButton.setText("Sair");
        logoutButton.setOnClickListener(v -> {
            authManager.realizarLogout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
