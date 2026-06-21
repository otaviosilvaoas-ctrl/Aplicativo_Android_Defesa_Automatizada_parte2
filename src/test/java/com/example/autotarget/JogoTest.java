package com.example.autotarget;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

/**
 * Testes Unitários para a AV1 e AV2 - AutoTarget
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
        jogo.setDimensoes(1000, 1000);
        
        // Tenta adicionar 21 canhões (o limite atual é 20)
        try {
            for (int i = 0; i <= 20; i++) {
                // Posiciona os canhões de forma a não colidirem e não ficarem na linha central
                jogo.adicionarCanhao(10 * i, 500); 
            }
            fail("Deveria ter lançado JogoException ao passar de 20 canhões");
        } catch (JogoException e) {
            assertEquals("Máximo de 20 canhões atingido", e.getMessage());
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

    @Test
    public void testAlvoInicialmenteNaEsquerda() {
        Jogo jogo = new Jogo();
        jogo.setDimensoes(1000, 1000); // centroX = 500
        
        Alvo alvo = new AlvoComum(100, 500, 30, 5);
        jogo.adicionarAlvo(alvo);
        
        assertTrue("Alvo deveria estar na lista da esquerda", jogo.getAlvosEsquerda().contains(alvo));
        assertFalse("Alvo NÃO deveria estar na lista da direita", jogo.getAlvosDireita().contains(alvo));
    }

    @Test
    public void testAlvoInicialmenteNaDireita() {
        Jogo jogo = new Jogo();
        jogo.setDimensoes(1000, 1000); // centroX = 500
        
        Alvo alvo = new AlvoComum(600, 500, 30, 5);
        jogo.adicionarAlvo(alvo);
        
        assertTrue("Alvo deveria estar na lista da direita", jogo.getAlvosDireita().contains(alvo));
        assertFalse("Alvo NÃO deveria estar na lista da esquerda", jogo.getAlvosEsquerda().contains(alvo));
    }

    @Test
    public void testTransferenciaEsquerdaParaDireita() {
        Jogo jogo = new Jogo();
        jogo.setDimensoes(1000, 1000); // centroX = 500
        
        Alvo alvo = new AlvoComum(100, 500, 30, 5);
        jogo.adicionarAlvo(alvo);
        
        // Simula movimento para a direita (atravessa 500)
        alvo.x = 600;
        jogo.atualizarPertencimentoDosAlvos();
        
        assertFalse("Alvo deveria ter saído da esquerda", jogo.getAlvosEsquerda().contains(alvo));
        assertTrue("Alvo deveria ter entrado na direita", jogo.getAlvosDireita().contains(alvo));
    }

    @Test
    public void testTransferenciaDireitaParaEsquerda() {
        Jogo jogo = new Jogo();
        jogo.setDimensoes(1000, 1000); // centroX = 500
        
        Alvo alvo = new AlvoComum(600, 500, 30, 5);
        jogo.adicionarAlvo(alvo);
        
        // Simula movimento para a esquerda (atravessa 500)
        alvo.x = 100;
        jogo.atualizarPertencimentoDosAlvos();
        
        assertFalse("Alvo deveria ter saído da direita", jogo.getAlvosDireita().contains(alvo));
        assertTrue("Alvo deveria ter entrado na esquerda", jogo.getAlvosEsquerda().contains(alvo));
    }

    @Test
    public void testAlvoNaoDuplicadoNasListas() {
        Jogo jogo = new Jogo();
        jogo.setDimensoes(1000, 1000);
        
        Alvo alvo = new AlvoComum(100, 500, 30, 5);
        jogo.adicionarAlvo(alvo);
        
        int totalNasListas = jogo.getAlvosEsquerda().size() + jogo.getAlvosDireita().size();
        assertEquals("Alvo deve existir em apenas uma das listas laterais", 1, totalNasListas);
        
        alvo.x = 600;
        jogo.atualizarPertencimentoDosAlvos();
        
        totalNasListas = jogo.getAlvosEsquerda().size() + jogo.getAlvosDireita().size();
        assertEquals("Após transferência, alvo ainda deve estar em apenas uma lista", 1, totalNasListas);
    }

    @Test
    public void testAlvoNaLinhaCentral() {
        Jogo jogo = new Jogo();
        jogo.setDimensoes(1000, 1000); // centroX = 500
        
        // Regra implementada: x < centroX -> Esquerda | x >= centroX -> Direita
        Alvo alvo = new AlvoComum(500, 500, 30, 5);
        jogo.adicionarAlvo(alvo);
        
        assertTrue("Na linha central (x=500), alvo deve pertencer à DIREITA", jogo.getAlvosDireita().contains(alvo));
        assertFalse("Na linha central, alvo NÃO deve estar na ESQUERDA", jogo.getAlvosEsquerda().contains(alvo));
    }
}
