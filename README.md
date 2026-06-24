# 🎯 AutoTarget - AV3 (Automação Avançada - UFLA)

## 📌 Descrição do Projeto

O **AutoTarget** é um jogo Android que simula um sistema automatizado de defesa em tempo real. Alvos se movimentam continuamente pelo campo de batalha enquanto canhões autônomos detectam, rastreiam e eliminam os alvos de forma automática.

Esta versão corresponde à **AV3 da disciplina GAT108 – Automação Avançada**, com foco na implementação de mecanismos de segurança, autenticação de usuários e proteção dos dados do sistema utilizando os serviços do Firebase.

---

## 🔐 Funcionalidades de Segurança Implementadas

### 👤 Autenticação de Usuários

* Integração com Firebase Authentication
* Cadastro de novos usuários por e-mail e senha
* Login seguro para acesso ao sistema
* Logout de usuários autenticados
* Persistência de sessão entre execuções do aplicativo

### 🛡️ Controle de Acesso

* Restrição de acesso ao jogo apenas para usuários autenticados
* Proteção da tela de ranking
* Validação automática da autenticação ao iniciar o aplicativo
* Redirecionamento para a tela de login quando necessário

### ☁️ Integração com Firebase

* Comunicação segura com os serviços Firebase
* Armazenamento de informações vinculadas ao usuário autenticado
* Identificação única de cada jogador através do UID fornecido pelo Firebase

### ⚠️ Tratamento de Falhas de Segurança

* Validação dos dados fornecidos no cadastro
* Tratamento de erros de autenticação
* Mensagens informativas para falhas de login
* Proteção contra acessos não autorizados

---

## 🎮 Funcionalidades Herdadas das Versões Anteriores

### 🎯 Sistema de Jogo

#### 🔵 Alvos

* Movimentação automática e aleatória
* Execução em threads independentes
* Distribuição entre os lados do campo de batalha

#### 🔺 Canhões

* Mira automática em alvos válidos
* Disparo condicionado à disponibilidade de energia
* Penalidade operacional por excesso de canhões

#### 💥 Projéteis

* Movimentação linear
* Detecção de colisão
* Atualização automática do placar
* Reutilização por Object Pool

### ⚡ Sistema de Energia

* Controle individual por equipe
* Consumo de energia a cada disparo
* Implementação thread-safe utilizando AtomicInteger

### 📡 Sistema de Sensores

* Aquisição periódica de dados dos alvos
* Frequência de amostragem de 1 Hz
* Inserção de ruído gaussiano de 5%
* Buffer circular para armazenamento das medições

### 🏆 Sistema de Pontuação

* Placar independente por equipe
* Determinação automática do vencedor
* Exibição do resultado final ao término da partida

---

## ⚙️ Arquitetura do Sistema

### 📦 Classes Principais

* `LoginActivity` → Tela de autenticação
* `RegisterActivity` → Cadastro de usuários
* `FirebaseAuthManager` → Gerenciamento da autenticação
* `Jogo` → Controle central do sistema
* `Alvo` → Gerenciamento dos alvos
* `Canhao` → Controle dos canhões
* `Projetil` → Gerenciamento dos projéteis
* `SensorManager` → Sistema de sensores
* `GameOverActivity` → Resultado final da partida

---

## 🔒 Testes de Segurança Realizados

* Verificação de login obrigatório
* Teste de bloqueio de acesso sem autenticação
* Teste de cadastro de novos usuários
* Teste de persistência de sessão
* Teste de logout e encerramento da sessão
* Validação do acesso ao ranking apenas para usuários autenticados
* Testes de tratamento de erros de autenticação

---

## 🚀 Melhorias em Relação à AV2

* Implementação do Firebase Authentication
* Sistema completo de cadastro e login
* Proteção das funcionalidades críticas do sistema
* Controle de acesso baseado em autenticação
* Persistência segura da sessão do usuário
* Tratamento de exceções relacionadas à segurança
* Testes de autenticação e autorização

---

## ✅ Conclusão

A versão AV3 amplia a confiabilidade e a segurança do AutoTarget por meio da implementação de autentação de usuários e controle de acesso utilizando Firebase Authentication. Com isso, o sistema passa a garantir que apenas usuários autorizados possam acessar funcionalidades importantes, tornando a aplicação mais robusta e alinhada aos princípios de segurança em sistemas automatizados.
