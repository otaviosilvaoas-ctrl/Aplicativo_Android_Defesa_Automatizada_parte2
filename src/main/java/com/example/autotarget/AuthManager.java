package com.example.autotarget;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * AV3 Letra E: Gerenciador de Autenticação Firebase (Testes de Segurança).
 */
public class AuthManager {

    private final FirebaseAuth mAuth;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(Exception e);
    }

    public AuthManager() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void cadastrarUsuario(String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess(authResult.getUser()))
                .addOnFailureListener(callback::onError);
    }

    public void realizarLogin(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess(authResult.getUser()))
                .addOnFailureListener(callback::onError);
    }

    public void realizarLogout() {
        mAuth.signOut();
    }

    public FirebaseUser getUsuarioAtual() {
        return mAuth.getCurrentUser();
    }

    public String getUsuarioAtualUid() {
        FirebaseUser user = mAuth.getCurrentUser();
        return (user != null) ? user.getUid() : "anonimo";
    }

    public boolean isUsuarioLogado() {
        return mAuth.getCurrentUser() != null;
    }
}
