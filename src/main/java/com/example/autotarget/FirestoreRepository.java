package com.example.autotarget;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repositório central para operações no Firebase Firestore.
 */
public class FirestoreRepository {

    private final FirebaseFirestore db;
    private final ExecutorService executor;

    public interface RankingCallback {
        void onSuccess(List<Partida> partidas);
        void onError(Exception e);
    }

    public FirestoreRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Salva uma partida na coleção "partidas" usando o ID da partida como ID do documento.
     */
    public void salvarPartida(Partida partida) {
        executor.execute(() -> {
            if (partida.getId() != null && !partida.getId().isEmpty()) {
                db.collection("partidas")
                        .document(partida.getId())
                        .set(partida)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FirestoreRepository", "Partida salva com sucesso! ID: " + partida.getId() + 
                                    ", Pontuacao: " + partida.getPontuacao() + 
                                    ", Timestamp: " + partida.getTimestamp());
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreRepository", "Erro ao salvar partida ID: " + partida.getId(), e);
                        });
            }
        });
    }

    /**
     * Busca as 10 melhores pontuações na coleção "partidas".
     */
    public void buscarRanking(RankingCallback callback) {
        executor.execute(() -> {
            db.collection("partidas")
                    .orderBy("pontuacao", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Partida> partidas = queryDocumentSnapshots.toObjects(Partida.class);
                        callback.onSuccess(partidas);
                    })
                    .addOnFailureListener(callback::onError);
        });
    }

    /**
     * Encerra o executor de tarefas em segundo plano.
     */
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
