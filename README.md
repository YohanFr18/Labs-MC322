# Smart Foundry - Planta Industrial

## Sobre o Projeto
A **Smart Foundry** é um sistema de simulação de uma linha de produção de processadores (wafers de silício para chips), desenvolvido no âmbito da disciplina MC322 (Programação Orientada a Objetos) da Unicamp. Este projeto implementa conceitos de orientação a objetos, auditoria de máquinas, estratégias de produção, composição, tratamento robusto de erros com base em regras de negócio, persistência do estado da planta em XML e testes automatizados com JUnit 5.

Dupla: Victor Rimes (301314) e Yohan Rocha (238967).

## Como Executar
Este projeto utiliza o **Gradle Wrapper**, portanto não é necessário ter o Gradle instalado previamente na máquina do avaliador. Na primeira execução, o Gradle baixa o JUnit do Maven Central (é preciso internet).

**Requisito:** JDK 17 ou superior (o Gradle 9 não roda em versões anteriores do Java).

### No Linux ou macOS
Abra o terminal na pasta raiz do projeto e execute os seguintes comandos:
* **Para compilar o projeto e rodar os testes:** `./gradlew build`
* **Para rodar só os testes automatizados:** `./gradlew test`
* **Para gerar a documentação Javadoc:** `./gradlew javadoc` (os arquivos são gerados na pasta `build/docs/javadoc`)
* **Para executar a aplicação:** `./gradlew run`

### No Windows
Abra o Prompt de Comando (cmd) ou o PowerShell na pasta raiz e execute:
* **Para compilar e rodar os testes:** `gradlew.bat build`
* **Para rodar só os testes:** `gradlew.bat test`
* **Para gerar a documentação:** `gradlew.bat javadoc`
* **Para executar a aplicação:** `gradlew.bat run`

### Relatório dos testes
Depois de `./gradlew test` (ou `build`), o relatório HTML do JUnit fica em
`build/reports/tests/test/index.html`. O terminal também mostra cada teste como `PASSED` ou `FAILED`.

## Estrutura de Pacotes
Todo o código fica sob `br.unicamp.ic.mc322`, na estrutura padrão do Gradle:

```
src/main/java/br/unicamp/ic/mc322/
├── Main.java        Interface de terminal: menus, leitura da entrada e tratamento das exceções
├── model/           Domínio: matéria-prima, demandas, produtos, máquinas e suas partes,
│                    linha de produção, enums (Cenario, StatusDemanda, StatusProduto) e Auditavel
├── service/         GerenciadorProducao: regras de negócio da planta (fabricar, comprar, manutenção)
├── strategy/        Padrão Strategy: EstrategiaProducao e as três estratégias
├── persistence/     PersistenciaPlanta: leitura e escrita do estado em XML
└── exception/       Hierarquia de exceções de domínio (PlantaException e subclasses)

src/test/java/br/unicamp/ic/mc322/
├── model/           Testes das regras das entidades
├── service/         Testes das regras de negócio do gerenciador
└── persistence/     Testes de salvar/carregar e dos erros de arquivo
```

As dependências seguem uma direção só: `model` não conhece nenhum outro pacote do projeto além de `exception`; `service` usa `model`; `persistence` usa `model` e `service`; e só a `Main` conhece todos. As partes internas das máquinas (`MotorEsteira`, `CamaraVaco`, `SensorTermico`) têm métodos de restauração *package-private*, visíveis apenas dentro de `model`, para que só a própria `Maquina` consiga alterá-las.

## Persistência do Estado da Planta
O menu principal tem duas opções novas:

* **8 - Salvar estado da planta:** grava o estado atual em `dados/planta.xml`, substituindo o anterior.
* **9 - Carregar estado da planta:** pede confirmação e substitui a planta em uso pela salva no arquivo.

**Comportamento ao iniciar:** o programa tenta carregar `dados/planta.xml`.
* Se o arquivo **não existe** (primeira execução), isso é tratado como normal: o programa pede o cenário e cria uma planta nova com os dados padrão do projeto.
* Se o arquivo **existe mas está inválido**, o erro é mostrado, o arquivo é mantido intacto (ele só é sobrescrito se o usuário salvar) e uma planta nova é criada.

Sair do programa não salva nem apaga nada: o último estado salvo continua disponível na próxima execução. A pasta `dados/` está no `.gitignore`, porque é gerada pela execução.

**Exemplo de arquivo** (trecho):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<planta>
  <cenario>IDEAL</cenario>
  <budget>49775.0</budget>
  <materiaPrima id="SIW-001">
    <nome>Wafer de Silício</nome>
    <quantidade>4880</quantidade>
    <unidade>mm2</unidade>
    <quantidadeMinima>100</quantidadeMinima>
    <custoPorUnidade>10</custoPorUnidade>
  </materiaPrima>
  <linhaDeProducao nome="Linha Principal">
    <maquina ligada="true" saude="98.97" tipo="LITOGRAFIA">
      <motorEsteira desgaste="2.5"/>
      <camaraVaco integridadeVedacoes="98.2" pressurizada="true"/>
      <sensores>
        <sensorTermico calibracao="99.0"/>
        <!-- ... um elemento por sensor (a litografia tem 4) -->
      </sensores>
    </maquina>
    <!-- ... ENCAPSULAMENTO e METROLOGIA -->
  </linhaDeProducao>
  <demandas>
    <demanda modelo="Turing-X4" quantidade="1" status="PENDENTE"/>
    <!-- ... -->
  </demandas>
</planta>
```

**Como o carregamento preserva a consistência:**
* `PersistenciaPlanta.carregar()` nunca altera a planta em uso: ele monta uma planta **nova** e só a devolve se todos os dados forem válidos. A `Main` só troca a referência depois que o carregamento termina sem exceção, então uma falha deixa a planta anterior intacta.
* As máquinas são recriadas pelos seus construtores normais, que criam as próprias partes (composição da Tarefa 5). Depois, o estado salvo é aplicado por métodos de delegação da `Maquina` (`restaurarMotor`, `restaurarCamara`, `restaurarCalibracaoSensor`). Nenhuma parte é criada fora da máquina nem exposta por um getter.
* Os valores lidos passam pelas mesmas regras dos objetos criados pelo programa: estoque e reserva não negativos, saúde e calibrações entre 0 e 100, câmara pressurizada só com vedações acima de 20%, número de sensores igual ao da estação, status de demanda compatível com a quantidade.
* A gravação é feita num arquivo temporário que só depois substitui o original, para que uma falha no meio da escrita não corrompa o último estado salvo.

**Erros tratados** (todos sinalizados com `PersistenciaException`, subclasse de `PlantaException`, com mensagem legível): arquivo inexistente, arquivo vazio, XML malformado ou corrompido, elemento raiz diferente de `<planta>`, elemento ou atributo ausente, número ou booleano inválido, cenário/status/tipo de máquina desconhecido, dado que viola regra de domínio e falha de leitura ou escrita (`IOException`).

## Escopo e Limitações

### Dados persistidos vs. não persistidos

**Persistidos (obrigatórios pelo enunciado):**
* **Estoque de matéria-prima:** identificador, nome, quantidade, unidade, reserva mínima e custo por unidade do wafer.
* **Máquinas e subcomponentes:** para cada estação da linha, em ordem, o tipo, se está ligada, a saúde, o desgaste do motor da esteira, a integridade das vedações e a pressurização da câmara de vácuo e a calibração de cada sensor térmico.
* **Demandas:** modelo, unidades restantes e `StatusDemanda`.

**Persistidos (complementares, escolha da dupla):**
* **Cenário e budget:** sem eles a planta recarregada não seria comparável. O cenário define os multiplicadores de falha e desgaste, e o budget decide quais operações ainda são possíveis.
* **Nome da linha de produção.**

**Mantidos só em memória (não persistidos), e por quê:**
* **Configuração fixa das estações** (nome, capacidade, custo de operação, probabilidade base de falha, número de sensores): é definida pela classe de cada estação. O XML guarda o `tipo`, e a configuração vem do construtor correspondente. O número de sensores do arquivo é conferido contra o da estação, para detectar arquivos incompatíveis.
* **Armazém de produtos fabricados:** é o histórico de saída da sessão. Os IDs dos produtos (`CPU-1`, `CPU-2`, ...) vêm de um contador estático em `Produto`, e restaurá-los exigiria manipular esse contador para não gerar IDs repetidos. Por simplicidade (KISS), uma planta carregada começa com o armazém vazio.
* **Estratégia de produção ativa:** é uma preferência de operação, não um estado da planta. Ao carregar, a estratégia volta para Fila de Wafers, e o programa avisa isso.
* **Leitura atual de temperatura dos sensores:** é uma medição momentânea, refeita a cada ciclo. Só a calibração, que é o estado real do sensor, é salva.
* **Geradores aleatórios (`Random`) e multiplicador de falha das máquinas:** os geradores são recriados, e o multiplicador é reaplicado a partir do cenário salvo.
* **Estado `EM_PRODUCAO` de uma demanda:** só existe enquanto uma unidade atravessa a linha, nunca entre duas operações do menu. Um arquivo com esse status é recusado como inválido.

### Explicação da suíte de testes
São 55 testes JUnit 5, em 6 classes, executados por `./gradlew test` e também dentro de `./gradlew build`. Nenhum teste lê o teclado, depende de outro teste ou de uma execução anterior da aplicação.

| Classe de teste | O que avalia |
|---|---|
| `model.MateriaPrimaTest` | Regras de estoque: consumo, `RecursoInsuficienteException` sem alterar o estoque, reserva mínima e validação do construtor. |
| `model.DemandaTest` | Ciclo de vida PENDENTE → EM_PRODUCAO → PENDENTE/CONCLUIDA, cancelamento e reativação, `DemandaInvalidaException` para quantidade negativa e as combinações que `Demanda.restaurar` recusa. |
| `model.MaquinaTest` | Composição: uma parte em falha (motor, câmara ou mais da metade dos sensores) para a máquina, o desgaste chega a todas as partes, o reparo é em cascata e a restauração recusa estados impossíveis. |
| `model.LinhaDeProducaoTest` | Agregação: a lista de máquinas não pode ser alterada de fora e a linha pede manutenção se alguma máquina pedir. |
| `service.GerenciadorProducaoTest` | Regras de negócio: compra, fabricação (wafer, budget e demanda), cancelamento por falta de wafer sem consumir nada, máquina desligada ou com parte quebrada (`MaquinaIndisponivelException`), manutenção "tudo ou nada" e produção sem estratégia. |
| `persistence.PersistenciaPlantaTest` | Ida e volta: salvar, carregar e comparar cenário, budget, estoque, cada máquina com suas partes e cada demanda com seu status; salvar de novo atualiza o arquivo; a planta carregada continua operando. Erros: arquivo inexistente, vazio, malformado, de outro formato, elemento ausente, número inválido, regra de domínio violada, status desconhecido ou `EM_PRODUCAO`, número de sensores incompatível, falha de escrita e a garantia de que uma falha no carregamento não altera a planta em uso. |

**Estratégia adotada:**
* **Comportamento, não detalhes internos:** os testes usam apenas métodos públicos e verificam resultados e exceções observáveis.
* **Exceções verificadas com `assertThrows`:** além do tipo da exceção, os testes conferem que nada foi alterado (estoque, budget, status) quando a operação falha, que é a garantia de validar antes de alterar da Tarefa 4.
* **Isolamento de arquivos:** os testes de persistência usam `@TempDir`. O JUnit cria uma pasta temporária para cada teste e a apaga no fim, então o `dados/planta.xml` real nunca é tocado.
* **Independência:** cada teste monta seus próprios objetos (`@BeforeEach` ou métodos auxiliares), e a ordem de execução não importa.
* **Aleatoriedade sob controle:** as falhas sorteadas pelas máquinas só mudam o risco e a aprovação dos produtos. Os testes verificam o que não depende de sorte (estoque, budget, status das demandas, estado das partes), para que o resultado seja sempre o mesmo.
