package com.example.autotarget;

import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repositório central para operações no Firebase Firestore com suporte a Criptografia (AV3 Letra B).
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
     * Salva uma partida criptografando os dados sensíveis antes de enviar ao Firestore.
     */
    public void salvarPartida(Partida partida) {
        executor.execute(() -> {
            try {
                // Agrupa campos sensíveis em um JSON
                JSONObject sensitiveJson = new JSONObject();
                sensitiveJson.put("usuarioId", partida.getUsuarioId());
                sensitiveJson.put("pontuacao", partida.getPontuacao());
                sensitiveJson.put("alvosAbatidos", partida.getAlvosAbatidos());
                sensitiveJson.put("quantidadeCanhoes", partida.getQuantidadeCanhoes());
                sensitiveJson.put("energiaRestante", partida.getEnergiaRestante());

                // Criptografa o JSON
                String encryptedPayload = Cryptography.encrypt(sensitiveJson.toString());

                // Monta o documento final (id e timestamp ficam abertos para controle básico)
                Map<String, Object> docData = new HashMap<>();
                docData.put("id", partida.getId());
                docData.put("timestamp", partida.getTimestamp());
                docData.put("payload", encryptedPayload);

                db.collection("partidas")
                        .document(partida.getId())
                        .set(docData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FirestoreRepository", "Partida criptografada salva com sucesso! ID: " + partida.getId());
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreRepository", "Erro ao salvar partida criptografada", e);
                        });
            } catch (Exception e) {
                Log.e("FirestoreRepository", "Erro no processo de criptografia/salvamento", e);
            }
        });
    }

    /**
     * Busca os registros, descriptografa os dados e reconstrói o ranking localmente.
     */
    public void buscarRanking(RankingCallback callback) {
        executor.execute(() -> {
            db.collection("partidas")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Partida> ranking = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            try {
                                String payload = doc.getString("payload");
                                if (payload != null) {
                                    // Descriptografia
                                    String decryptedJson = Cryptography.decrypt(payload);
                                    if (decryptedJson != null) {
                                        JSONObject json = new JSONObject(decryptedJson);
                                        
                                        Partida p = new Partida(
                                                doc.getString("id"),
                                                doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0,
                                                json.getInt("pontuacao"),
                                                json.getInt("alvosAbatidos"),
                                                json.getInt("quantidadeCanhoes"),
                                                json.getInt("energiaRestante"),
                                                json.getString("usuarioId")
                                        );
                                        ranking.add(p);
                                    }
                                } else {
                                    // Caso existam partidas antigas não criptografadas, tenta ler normalmente para não quebrar o app
                                    Partida p = doc.toObject(Partida.class);
                                    if (p != null) ranking.add(p);
                                }
                            } catch (Exception e) {
                                Log.e("FirestoreRepository", "Falha ao processar documento", e);
                            }
                        }
                        
                        // Ordenação por pontuação decrescente (necessário fazer em memória pois o campo está criptografado no Firestore)
                        Collections.sort(ranking, (p1, p2) -> Integer.compare(p2.getPontuacao(), p1.getPontuacao()));
                        
                        // Retorna os top 10
                        int limit = Math.min(10, ranking.size());
                        callback.onSuccess(new ArrayList<>(ranking.subList(0, limit)));
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
