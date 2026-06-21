# 🎯 AutoTarget - AV2 (Automação Avançada - UFLA)

## 📌 Descrição do Projeto

O **AutoTarget** é um jogo desenvolvido para dispositivos Android que simula um sistema de defesa automatizado em tempo real. Alvos se movimentam continuamente pelo campo de batalha enquanto canhões autônomos realizam a detecção, rastreamento e eliminação dos alvos de forma automática.

Esta versão corresponde à **AV2 da disciplina GAT108 – Automação Avançada**, expandindo a base desenvolvida na AV1 com conceitos de:

* Programação concorrente
* Sincronização entre múltiplas threads
* Sistemas de sensores
* Gerenciamento de recursos
* Penalidades operacionais
* Divisão lógica do campo de batalha
* Programação Orientada a Objetos (POO)

---

## 🎮 Funcionalidades Implementadas

### 🖥️ Interface (UI)

* Canvas com renderização em tempo real
* Botão **Iniciar**
* Botão **Adicionar Canhão**
* Sistema de placar por equipe
* Exibição do nível de energia de cada lado
* Indicação visual de penalidades por excesso de canhões
* Tela de fim de jogo com resultado final

---

### 🎯 Sistema de Jogo

#### 🔵 Alvos

* Representados por círculos em movimento
* Movimentação automática e aleatória
* Implementados como threads independentes
* Distribuídos entre os lados esquerdo e direito do campo

#### 🔺 Canhões

* Representados por triângulos
* Executados em threads próprias
* Mira automática em alvos válidos do mesmo lado
* Disparo condicionado à disponibilidade de energia
* Penalidade dinâmica na taxa de disparo quando há excesso de canhões

#### 💥 Projéteis

* Movimentação linear em direção ao alvo
* Detecção de colisão
* Atualização automática do placar
* Reutilização através de Object Pool para redução de overhead

---

### ⚡ Sistema de Energia

* Cada lado inicia com 100 unidades de energia
* Cada disparo consome energia
* Controle thread-safe utilizando AtomicInteger
* Bloqueio automático de disparos quando a energia se esgota
* Atualização visual em tempo real

---

### 📡 Sistema de Sensores

* Coleta periódica de dados dos alvos
* Frequência de amostragem de 1 Hz
* Inserção de ruído gaussiano de 5%
* Armazenamento das leituras em buffer circular
* Histórico de até 20 medições por alvo

---

### 🏆 Sistema de Pontuação

* Pontuação independente para cada lado
* Incremento automático quando um alvo é abatido
* Determinação automática de vencedor ou empate

---

## ⚙️ Arquitetura do Sistema

### 📦 Classes Principais

* `Jogo` → Gerenciamento central do sistema
* `Alvo` → Controle dos alvos móveis
* `Canhao` → Controle dos canhões automáticos
* `Projetil` → Gerenciamento dos projéteis
* `SensorManager` → Coleta de dados dos sensores
* `GameOverActivity` → Exibição do resultado final

---

## 🧵 Programação Concorrente

O sistema utiliza múltiplas threads executando simultaneamente:

* Threads de movimentação dos alvos
* Threads dos canhões
* Threads dos projéteis
* Thread de aquisição de sensores
* Atualizações sincronizadas do estado do jogo

---

## 🔒 Sincronização

Para evitar condições de corrida:

* Uso de `synchronized` em regiões críticas
* Proteção das listas compartilhadas
* Controle seguro de energia utilizando `AtomicInteger`
* Sincronização de buffers de sensores
* Controle seguro de acesso aos objetos reutilizados

---

## 🚀 Melhorias em Relação à AV1

* Correção do problema de double-start de threads
* Controle de qualquer canhão por toque
* Limites dinâmicos de tela
* Implementação de Object Pool para projéteis
* Sistema de energia por equipe
* Penalidade operacional por excesso de canhões
* Sistema de sensores com ruído gaussiano
* Buffer circular de medições
* Divisão lógica do campo de batalha
* Sistema de pontuação e determinação automática de vencedor

---

## ⚠️ Tratamento de Exceções

* Utilização de blocos `try-catch`
* Tratamento de falhas de execução concorrente
* Exceções personalizadas para validação das regras do jogo
* Proteção contra estados inválidos do sistema
