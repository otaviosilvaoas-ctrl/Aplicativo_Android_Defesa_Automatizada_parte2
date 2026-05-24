package com.example.autotarget;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testes Unitários para a AV1 - AutoTarget
 */
public class JogoTest {

    @Test
    public void testDetectarColisao() {
        // Alvo na posição (100, 100) com raio 30
        Alvo alvo = new AlvoComum(100, 100, 30, 0);
        
        // Projetil exatamente em cima (100, 100)
        Projetil p1 = new Projetil(100, 100, 0, 0);
        assertTrue("Deveria detectar colisão (em cima)", alvo.verificarColisao(p1));
        
        // Projetil longe (300, 300)
        Projetil p2 = new Projetil(300, 300, 0, 0);
        assertFalse("Não deveria detectar colisão (longe)", alvo.verificarColisao(p2));
    }

    @Test
    public void testLimiteDeCanhoes() {
        Jogo jogo = new Jogo();
        
        // Tenta adicionar 11 canhões (o limite é 10)
        try {
            for (int i = 0; i <= 10; i++) {
                jogo.adicionarCanhao(100 * i, 100);
            }
            fail("Deveria ter lançado JogoException ao passar de 10 canhões");
        } catch (JogoException e) {
            assertEquals("Máximo de 10 canhões atingido", e.getMessage());
        }
    }

    @Test
    public void testInativarProjetilForaDaTela() {
        // Projetil criado na borda direita (1080) e movendo para fora
        Projetil p = new Projetil(1080, 500, 0, 10); 
        p.setLimitesTela(1080, 1920);
        
        assertTrue("Deve começar ativo", p.isAtivo());
        
        p.mover(); // Move x para 1090 (fora da largura 1080)
        
        assertFalse("Deve ficar inativo ao sair da tela", p.isAtivo());
    }
}