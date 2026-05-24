# 🎯 AutoTarget - AV1 (Automação Avançada - UFLA)

## 📌 Descrição do Projeto
O **AutoTarget** é um jogo desenvolvido para dispositivos Android que simula um sistema de defesa automatizado. Alvos se movimentam pela tela e canhões automáticos disparam projéteis para abatê-los.

Este projeto foi desenvolvido como parte da **AV1 da disciplina GAT108 – Automação Avançada**, com foco em:
- Programação concorrente
- Threads
- Sincronização
- Programação Orientada a Objetos (POO)

---

## 🎮 Funcionalidades Implementadas

### 🖥️ Interface (UI)
- Canvas com renderização dos alvos em movimento
- Botão **Iniciar** para iniciar o jogo
- Botão **Adicionar Canhão** para posicionar canhões na tela

---

### 🎯 Sistema de Jogo

#### 🔵 Alvos
- Representados por círculos
- Movimentação automática e aleatória
- Implementados como **threads independentes**

#### 🔺 Canhões
- Representados por triângulos
- Cada canhão roda em uma **thread própria**
- Disparo automático de projéteis
- Mira no **alvo mais próximo**

#### 💥 Projéteis
- Movimento em linha reta
- Verificação de colisão com alvos
- Executados em **threads independentes**

---

## ⚙️ Arquitetura do Sistema

### 📦 Classes principais
- `Jogo` → Gerencia o estado geral do jogo
- `Alvo` → Thread responsável pelo movimento dos alvos
- `Canhao` → Thread responsável pelos disparos automáticos
- `Projetil` → Thread responsável pelo movimento dos projéteis

---

## 🧵 Programação Concorrente

O sistema utiliza múltiplas threads para simular comportamento em tempo real:

- Cada **alvo** é uma thread
- Cada **canhão** é uma thread
- Cada **projétil** é uma thread

---

## 🔒 Sincronização

Para evitar condições de corrida:

- Uso de `synchronized` para proteger:
  - Lista de alvos
  - Lista de projéteis

- Região crítica implementada para:
  - Verificação de colisão (garantindo acesso exclusivo)

---

## ⚠️ Tratamento de Exceções

- Uso de blocos `try-catch` para evitar falhas
- Criação de exceção personalizada:

```java
public class JogoException extends Exception {
    public JogoException(String mensagem) {
        super(mensagem);
    }
}
