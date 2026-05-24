package com.example.autotarget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override // Estou criando minha própria versão do que acontece na tela
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // O compilador lê o XML que eu criei

        /* O código vai até o XML e procura o ID que eu passei !
         * Ele exibe o texto escrito no XML */
        TextView titleView = findViewById(R.id.title);
        titleView.setText(R.string.app_name);

        TextView subtitleView = findViewById(R.id.subtitle);
        subtitleView.setText(R.string.Subtitle);

        /* Coloca um sensor no botão que fica ouvindo até que alguém pressione-o.
           O intent cria um caminho da página atual para a outra indicada no código.
           O android abre a nova tela e coloca a atual em segundo plano.
        **/
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(view -> {
            // Usando GameViewActivity que é a classe que configuramos para o jogo
            Intent intent = new Intent(this, GameViewActivity.class);
            startActivity(intent);
        });
    }
}