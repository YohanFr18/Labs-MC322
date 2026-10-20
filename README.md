# Smart Foundry - Planta Industrial

## Sobre o Projeto
A **Smart Foundry** é um sistema de simulação de uma linha de produção de processadores (wafers de silício para chips), desenvolvido no âmbito da disciplina MC322 (Programação Orientada a Objetos) da Unicamp. Este projeto implementa conceitos de orientação a objetos, auditoria de máquinas, estratégias de produção e tratamento robusto de erros com base em regras de negócio.

## Como Executar
Este projeto utiliza o **Gradle Wrapper**, portanto não é necessário ter o Gradle instalado previamente na máquina do avaliador.

**Requisito:** JDK 17 ou superior (o Gradle 9 não roda em versões anteriores do Java).

### No Linux ou macOS
Abra o terminal na pasta raiz do projeto e execute os seguintes comandos:
* **Para compilar o projeto:** `./gradlew build`
* **Para gerar a documentação Javadoc:** `./gradlew javadoc` (os arquivos são gerados na pasta `build/docs/javadoc`)
* **Para executar a aplicação:** `./gradlew run`

### No Windows
Abra o Prompt de Comando (cmd) ou o PowerShell na pasta raiz e execute:
* **Para compilar:** `gradlew.bat build`
* **Para gerar a documentação:** `gradlew.bat javadoc`
* **Para executar a aplicação:** `gradlew.bat run`