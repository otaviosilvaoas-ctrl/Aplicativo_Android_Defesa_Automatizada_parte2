package com.example.autotarget;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repositório central para operações no Firebase Firestore.
 * Atende aos requisitos da AV3 Letra C (Sincronização), Letra B (Criptografia) e Letra E (Segurança).
 */
public class FirestoreRepository {

    private static final String TAG = "FirestoreRepository";
    private final FirebaseFirestore db;
    private final ExecutorService executor;
    private final Object dbLock = new Object();

    public interface RankingCallback {
        void onSuccess(List<Partida> partidas);
        void onError(Exception e);
    }

    public FirestoreRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * AV3 Letra E: Salva a partida vinculada ao UID do usuário autenticado.
     */
    public void salvarPartida(Partida partida) {
        Log.d(TAG, "AV3 E - Iniciando salvamento de partida para o usuário: " + partida.getUsuarioId());
        
        executor.execute(() -> {
            synchronized (dbLock) {
                try {
                    JSONObject sensitiveJson = new JSONObject();
                    sensitiveJson.put("usuarioId", partida.getUsuarioId());
                    sensitiveJson.put("pontuacao", partida.getPontuacao());
                    sensitiveJson.put("alvosAbatidos", partida.getAlvosAbatidos());
                    sensitiveJson.put("quantidadeCanhoes", partida.getQuantidadeCanhoes());
                    sensitiveJson.put("energiaRestante", partida.getEnergiaRestante());

                    // AV3 Letra B: Criptografia dos dados sensíveis
                    String encryptedPayload = Cryptography.encrypt(sensitiveJson.toString());

                    Map<String, Object> docData = new HashMap<>();
                    docData.put("id", partida.getId());
                    docData.put("timestamp", partida.getTimestamp());
                    docData.put("payload", encryptedPayload);
                    // AV3 Letra E: Campo plano para permitir regras de segurança do Firestore
                    docData.put("userId", partida.getUsuarioId());

                    db.collection("partidas")
                            .document(partida.getId())
                            .set(docData)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "AV3 E - Partida salva com sucesso."))
                            .addOnFailureListener(e -> Log.e(TAG, "AV3 E - Erro ao salvar partida.", e));
                } catch (Exception e) {
                    Log.e(TAG, "Erro no processo de salvamento", e);
                }
            }
        });
    }

    /**
     * AV3 Letra E: Busca apenas as partidas do usuário logado.
     */
    public void buscarRanking(RankingCallback callback) {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) {
            callback.onError(new Exception("Usuário não autenticado"));
            return;
        }

        Log.d(TAG, "AV3 E - Buscando partidas apenas do usuário: " + currentUid);

        executor.execute(() -> {
            synchronized (dbLock) {
                // AV3 Letra E: Filtro por userId no Firestore para garantir privacidade
                db.collection("partidas")
                        .whereEqualTo("userId", currentUid)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            List<Partida> ranking = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                try {
                                    String payload = doc.getString("payload");
                                    if (payload != null) {
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
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Erro ao processar documento", e);
                                }
                            }
                            // Ordenação em memória (Letra B)
                            Collections.sort(ranking, (p1, p2) -> Integer.compare(p2.getPontuacao(), p1.getPontuacao()));
                            int limit = Math.min(10, ranking.size());
                            callback.onSuccess(new ArrayList<>(ranking.subList(0, limit)));
                        })
                        .addOnFailureListener(callback::onError);
            }
        });
    }

    public void salvarTelemetria(Telemetria telemetria) {
        executor.execute(() -> {
            synchronized (dbLock) {
                db.collection("telemetria")
                        .add(telemetria)
                        .addOnSuccessListener(documentReference -> Log.d(TAG, "AV3 D - Telemetria salva."))
                        .addOnFailureListener(e -> Log.e(TAG, "AV3 D - Erro na telemetria", e));
            }
        });
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
