package br.unicamp.ic.mc322;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.exception.PlantaException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.persistence.PersistenciaPlanta;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.strategy.EstrategiaFilaDeWafers;
import br.unicamp.ic.mc322.strategy.EstrategiaLoteMaximo;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;
import br.unicamp.ic.mc322.strategy.EstrategiaRendimentoMaximo;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Classe principal que contém o ponto de entrada da aplicação e o loop de menus interativos da Smart Foundry.
 * É responsável por interagir com o usuário, validar a entrada de dados (evitando falhas por tipos incorretos) 
 * e tratar adequadamente as exceções de domínio geradas pelo sistema.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class Main {

    // Arquivo onde o estado da planta é salvo, relativo à pasta de onde o programa
    // é executado (com ./gradlew run, a raiz do projeto)
    private static final Path ARQUIVO_ESTADO = Path.of("dados", "planta.xml");

    // A Main só tem métodos estáticos, então não deve ser instanciada
    private Main() {
    }
    
    /**
     * Lê a entrada do usuário garantindo que apenas números inteiros são aceitos,
     * impedindo erros de InputMismatchException. Também previne falhas abruptas por fim de arquivo (EOF / Ctrl+D).
     *
     * @param sc Scanner utilizado para ler os dados do console.
     * @param msg Mensagem apresentada ao usuário antes de aguardar a entrada.
     * @return O número inteiro digitado e validado.
     */
    static int lerInteiro(Scanner sc, String msg) {
        System.out.print(msg);
        while (!sc.hasNextInt()) {
            if (!sc.hasNext()) { 
                System.out.println("\nEntrada encerrada. Fechando a fábrica.");
                System.exit(0);
            }
            System.out.println("Entrada inválida. Digite apenas números inteiros.");
            sc.next();
            System.out.print(msg);
        }
        return sc.nextInt();
    }

    /**
     * Imprime o cabeçalho formatado de uma seção específica do menu.
     *
     * @param titulo Título da seção a ser impresso na tela.
     */
    static void imprimirSecao(String titulo) {
        System.out.println("--------------------------------------------------------------");
        System.out.println(" [" + titulo + "]");
        System.out.println("--------------------------------------------------------------");
    }

    /**
     * Exibe o cabeçalho principal da aplicação, mostrando o estado atual da fábrica,
     * incluindo a estratégia ativa, o cenário e o orçamento (budget) disponível.
     *
     * @param gerenciador Instância do gerenciador de produção que contém os dados atuais.
     */
    static void exibirCabecalho(GerenciadorProducao gerenciador) {
        System.out.println("==============================================================");
        System.out.println("  SMART FOUNDRY - Da areia ao algoritmo");
        System.out.println("--------------------------------------------------------------");
        System.out.println("  ESTRATÉGIA ATUAL : " + gerenciador.getNomeEstrategiaAtual());
        System.out.println("  CENÁRIO ATIVO    : " + gerenciador.getNomeCenarioAtivo());
        System.out.printf("  BUDGET ATUAL     : R$ %.2f%n", gerenciador.getBudget());
        System.out.println("==============================================================");
    }

    /**
     * Apresenta o menu inicial para o usuário escolher o cenário de dificuldade da fábrica.
     * Garante que o usuário escolhe uma opção válida antes de prosseguir.
     *
     * @param sc Scanner para ler a opção do usuário.
     * @return O {@link Cenario} selecionado (IDEAL ou APOCALIPTICO).
     */
    static Cenario escolherCenario(Scanner sc) {
        imprimirSecao("SELEÇÃO DE CENÁRIO");
        System.out.println("""
                1 - Ideal: mercado estável, budget farto, máquinas confiáveis e desgaste leve
                2 - Apocalíptico: crise do silício, budget mínimo, falhas frequentes e desgaste acelerado
                """);
        while (true) {
            int escolha = lerInteiro(sc, "Escolha o cenário (1-2): ");
            if (escolha == 1) {
                return Cenario.IDEAL;
            }
            if (escolha == 2) {
                return Cenario.APOCALIPTICO;
            }

            System.out.println("Cenário inválido. Por favor, escolha 1 ou 2.");
        }
    }

    /**
     * Apresenta o menu para seleção de um modelo específico de processador.
     *
     * @param sc Scanner para ler a opção do usuário.
     * @return A string correspondente ao nome do modelo selecionado, ou {@code null} se a opção for inválida.
     */
    static String escolherModelo(Scanner sc) {
        System.out.println("""
                1 - Turing-X4     (baixa qualidade)
                2 - Lovelace-X8   (média qualidade)
                3 - Torvalds-X16  (alta qualidade)
                """);
        int escolha = lerInteiro(sc, "Selecione o modelo (1-3): ");
        return switch (escolha) {
            case 1 -> "Turing-X4";
            case 2 -> "Lovelace-X8";
            case 3 -> "Torvalds-X16";
            default -> null;
        };
    }

    /**
     * Controla o submenu responsável por criar, atualizar e listar as demandas da fábrica.
     * Trata eventuais exceções decorrentes da introdução de quantidades inválidas.
     *
     * @param sc Scanner para ler as interações do usuário.
     * @param gerenciador Instância ativa do gerenciador de produção.
     */
    static void menuDemandas(Scanner sc, GerenciadorProducao gerenciador) {
        while (true) {
            imprimirSecao("DEMANDAS");
            System.out.println("""
                    1 - Atualizar demanda
                    2 - Listar demandas
                    0 - Voltar
                    """);
            int opcao = lerInteiro(sc, "Escolha: ");
            if (opcao == 0) {
                return;
            } else if (opcao == 1) {
                String modelo = escolherModelo(sc);
                if (modelo == null) {
                    System.out.println("Operação cancelada: O modelo selecionado não existe.");
                    continue;
                }
                
                int novaQtd = lerInteiro(sc, "Nova quantidade da demanda: ");
                if (novaQtd <= 0) {
                    System.out.println("Operação inválida: A quantidade deve ser maior que zero.");
                    continue;
                }
                
                try {
                    gerenciador.atualizarDemanda(modelo, novaQtd);
                } catch (DemandaInvalidaException e) {
                    System.out.println("ERRO ===\nNão foi possível atualizar a demanda: " + e.getMessage());
                }
                
            } else if (opcao == 2) {
                gerenciador.listarDemandas();
            } else {
                System.out.println("Opção inválida. Escolha uma opção do menu.");
            }
        }
    }

    /**
     * Controla o submenu de fabricação de processadores.
     * Captura as exceções de domínio da aplicação, permitindo a recuperação de erros como falta de recursos 
     * ou quebra de máquinas sem encerrar o programa abruptamente.
     *
     * @param sc Scanner para ler as interações do usuário.
     * @param gerenciador Instância ativa do gerenciador de produção.
     */
    static void menuFabricacao(Scanner sc, GerenciadorProducao gerenciador) {
        while (true) {
            imprimirSecao("FABRICAÇÃO");
            System.out.println("""
                    1 - Processar próxima demanda (usa a estratégia ativa)
                    2 - Fabricar modelo específico
                    0 - Voltar
                    """);
            int opcao = lerInteiro(sc, "Escolha: ");
            if (opcao == 0) {
                return;
            } else if (opcao == 1 || opcao == 2) {
                String modelo = null;
                if (opcao == 2) {
                    modelo = escolherModelo(sc);
                    if (modelo == null) {
                        System.out.println("Operação cancelada: O modelo selecionado não existe.");
                        continue;
                    }
                }

                try {
                    if (opcao == 1) {
                        gerenciador.executarProximaProducao();
                    } else {
                        gerenciador.fabricarDemanda(modelo);
                    }
                
                } catch (RecursoInsuficienteException e) {
                    System.out.println("ERRO ===\nProdução abortada por falta de recursos: " + e.getMessage());
                    System.out.println("Nenhuma alteração foi realizada no estoque ou orçamento.");
                } catch (MaquinaIndisponivelException e) {
                    System.out.println("ERRO ===\nProdução interrompida: " + e.getMessage());
                } catch (DemandaInvalidaException e) {
                    System.out.println("ERRO ===\n" + e.getMessage());
                } catch (PlantaException e) {
                    System.out.println("ERRO INESPERADO ===\n" + e.getMessage());
                }
            } else {
                System.out.println("Opção inexistente. Tente novamente.");
            }
        }
    }

    /**
     * Controla o submenu responsável por consultar o armazém de produtos finalizados e o estado do inventário de matérias-primas.
     *
     * @param sc Scanner para ler as interações do usuário.
     * @param gerenciador Instância ativa do gerenciador de produção.
     */
    static void menuConsultar(Scanner sc, GerenciadorProducao gerenciador) {
        while (true) {
            imprimirSecao("CONSULTAR");
            System.out.println("""
                    1 - Ver armazém (produtos acabados)
                    2 - Ver estoque de matéria-prima
                    0 - Voltar
                    """);
            int opcao = lerInteiro(sc, "Escolha: ");
            if (opcao == 0) {
                return;
            } else if (opcao == 1) {
                gerenciador.exibirArmazem();
            } else if (opcao == 2) {
                // Pedimos ao gerenciador a cada consulta: depois de um carregamento
                // a planta (e o estoque) é outra
                MateriaPrima wafer = gerenciador.getMateriaPrima();
                System.out.println(wafer.getNome() + ": " + wafer.getQuantidade() + " " + wafer.getUnidade()
                        + " (reserva mínima de segurança: " + wafer.getQuantidadeMinima() + " " + wafer.getUnidade() + ")");
            } else {
                System.out.println("Opção inválida.");
            }
        }
    }

    /**
     * Controla o submenu que permite alterar a estratégia de produção utilizada pela fábrica.
     *
     * @param sc Scanner para ler a opção selecionada.
     * @param gerenciador Instância ativa do gerenciador de produção a ser atualizada.
     */
    static void menuEstrategia(Scanner sc, GerenciadorProducao gerenciador) {
        imprimirSecao("GERENCIAR ESTRATÉGIA");
        System.out.println("""
                1 - Fila de Wafers    : atende as encomendas por ordem de chegada
                2 - Lote Máximo       : prioriza a maior encomenda pendente
                3 - Rendimento Máximo : maximiza a produção sem estourar o budget
                0 - Voltar
                """);
        int opcao = lerInteiro(sc, "Escolha: ");
        EstrategiaProducao novaEstrategia = switch (opcao) {
            case 1 -> new EstrategiaFilaDeWafers();
            case 2 -> new EstrategiaLoteMaximo();
            case 3 -> new EstrategiaRendimentoMaximo();
            default -> null;
        };
        
        if (novaEstrategia == null) {
            if (opcao != 0) {
                System.out.println("Estratégia inválida. Selecione uma opção existente.");
            }
            return;
        }
        gerenciador.setEstrategia(novaEstrategia);
        System.out.println("Estratégia alterada para: " + novaEstrategia.getNomeEstrategia());
    }

    /**
     * Controla o submenu da linha de produção: mostra as estações que a compõem,
     * permite ligar ou desligar cada uma e exibe a auditoria estrutural
     * (linha, estações e componentes internos).
     *
     * @param sc Scanner para ler as interações do usuário.
     * @param gerenciador Instância ativa do gerenciador de produção.
     */
    static void menuLinha(Scanner sc, GerenciadorProducao gerenciador) {
        while (true) {
            imprimirSecao("LINHA DE PRODUÇÃO");
            System.out.println("""
                    1 - Ver estações da linha
                    2 - Ligar/desligar uma estação
                    3 - Auditoria estrutural (Linha > Estações > Componentes)
                    0 - Voltar
                    """);
            int opcao = lerInteiro(sc, "Escolha: ");
            if (opcao == 0) {
                return;
            } else if (opcao == 1) {
                gerenciador.listarMaquinas();
            } else if (opcao == 2) {
                gerenciador.listarMaquinas();
                int total = gerenciador.getQuantidadeMaquinas();
                int numero = lerInteiro(sc, "Número da estação (1-" + total + "): ");
                if (numero < 1 || numero > total) {
                    System.out.println("Operação cancelada: essa estação não existe.");
                    continue;
                }
                gerenciador.alternarEstadoMaquina(numero);
            } else if (opcao == 3) {
                gerenciador.exibirAuditoriaEstrutural();
            } else {
                System.out.println("Opção inválida.");
            }
        }
    }

    /**
     * Controla o submenu focado na auditoria da fábrica, permitindo inspecionar máquinas, produtos
     * e executar a manutenção preventiva do equipamento.
     *
     * @param sc Scanner para ler as interações do usuário.
     * @param gerenciador Instância ativa do gerenciador de produção.
     */
    static void menuAuditoria(Scanner sc, GerenciadorProducao gerenciador) {
        while (true) {
            imprimirSecao("AUDITORIA E MANUTENÇÃO");
            System.out.println(" 1 - Relatório geral da planta");
            System.out.println(" 2 - Auditoria estrutural (Linha > Estações > Componentes)");
            System.out.println(" 3 - Detalhar produtos");
            System.out.printf(" 4 - Manutenção das máquinas (R$ %.2f por máquina)%n", GerenciadorProducao.CUSTO_MANUTENCAO);
            System.out.println(" 0 - Voltar");
            System.out.println();
            int opcao = lerInteiro(sc, "Escolha: ");
            
            if (opcao == 0) {
                return;
            } else if (opcao == 1) {
                gerenciador.gerarAuditoriaGeral();
            } else if (opcao == 2) {
                gerenciador.exibirAuditoriaEstrutural();
            } else if (opcao == 3) {
                gerenciador.detalharProdutos();
            } else if (opcao == 4) {
                try {
                    gerenciador.realizarManutencao();
                } catch (RecursoInsuficienteException e) {
                    System.out.println("ERRO ===\n" + e.getMessage());
                }
            } else {
                System.out.println("Opção inválida.");
            }
        }
    }

    /**
     * Cria uma planta nova com os dados padrão do projeto: estoque inicial de
     * wafer, as três demandas zeradas e a estratégia Fila de Wafers.
     *
     * @param cenario Cenário de operação escolhido.
     * @return A planta nova.
     */
    static GerenciadorProducao criarPlantaPadrao(Cenario cenario) {
        MateriaPrima wafer = new MateriaPrima("SIW-001", "Wafer de Silício", 5000, "mm2", 100, 10);
        GerenciadorProducao gerenciador = new GerenciadorProducao(wafer, cenario);
        gerenciador.setEstrategia(new EstrategiaFilaDeWafers());
        gerenciador.registrarDemanda(new Demanda("Turing-X4", 0));
        gerenciador.registrarDemanda(new Demanda("Lovelace-X8", 0));
        gerenciador.registrarDemanda(new Demanda("Torvalds-X16", 0));
        return gerenciador;
    }

    /**
     * Define a planta com que o programa começa. Se existir um estado salvo,
     * ele é carregado. Se não existir (primeira execução), isso é normal e uma
     * planta nova é criada. Se o arquivo existir mas estiver inválido, o erro é
     * mostrado, o arquivo é mantido intacto e uma planta nova é criada.
     *
     * @param sc Scanner para a escolha do cenário, se necessária.
     * @param persistencia Persistência ligada ao arquivo de estado.
     * @return A planta inicial.
     */
    static GerenciadorProducao iniciarPlanta(Scanner sc, PersistenciaPlanta persistencia) {
        if (persistencia.existeArquivo()) {
            try {
                GerenciadorProducao carregada = persistencia.carregar();
                carregada.setEstrategia(new EstrategiaFilaDeWafers());
                System.out.println("[ESTADO] Planta restaurada de " + persistencia.getArquivo()
                        + " (" + carregada.getNomeCenarioAtivo() + ").");
                return carregada;
            } catch (PersistenciaException e) {
                System.out.println("ERRO ===\nNão foi possível restaurar o estado salvo: " + e.getMessage());
                System.out.println("O arquivo não será alterado até você salvar. Iniciando uma planta nova.");
            }
        } else {
            System.out.println("[ESTADO] Nenhum estado salvo encontrado. Iniciando uma planta nova.");
        }
        return criarPlantaPadrao(escolherCenario(sc));
    }

    /**
     * Salva o estado atual da planta no arquivo, substituindo o estado anterior.
     *
     * @param persistencia Persistência ligada ao arquivo de estado.
     * @param gerenciador Planta a salvar.
     */
    static void salvarPlanta(PersistenciaPlanta persistencia, GerenciadorProducao gerenciador) {
        try {
            persistencia.salvar(gerenciador);
            System.out.println("[ESTADO] Planta salva em " + persistencia.getArquivo() + ".");
        } catch (PersistenciaException e) {
            System.out.println("ERRO ===\nNão foi possível salvar: " + e.getMessage());
        }
    }

    /**
     * Substitui a planta atual pela salva no arquivo, depois de confirmar com o
     * usuário. Se o carregamento falhar, a planta atual continua valendo.
     *
     * @param sc Scanner para a confirmação.
     * @param persistencia Persistência ligada ao arquivo de estado.
     * @param atual Planta em uso agora.
     * @return A planta carregada, ou a atual se nada foi carregado.
     */
    static GerenciadorProducao carregarPlanta(Scanner sc, PersistenciaPlanta persistencia, GerenciadorProducao atual) {
        if (!persistencia.existeArquivo()) {
            System.out.println("Nenhum estado salvo em " + persistencia.getArquivo() + ". Salve a planta primeiro.");
            return atual;
        }
        int confirmacao = lerInteiro(sc, "Carregar substitui a planta atual e descarta o que não foi salvo. "
                + "Confirmar? (1 - sim, 0 - não): ");
        if (confirmacao != 1) {
            System.out.println("Carregamento cancelado.");
            return atual;
        }
        try {
            GerenciadorProducao carregada = persistencia.carregar();
            carregada.setEstrategia(new EstrategiaFilaDeWafers());
            System.out.println("[ESTADO] Planta carregada de " + persistencia.getArquivo()
                    + ". A estratégia voltou para Fila de Wafers e o armazém começa vazio.");
            return carregada;
        } catch (PersistenciaException e) {
            System.out.println("ERRO ===\nNão foi possível carregar: " + e.getMessage());
            System.out.println("Nada foi alterado: a planta atual continua em uso.");
            return atual;
        }
    }

    /**
     * Método principal responsável por iniciar a aplicação, configurar os componentes da fábrica
     * e executar o loop do menu central.
     *
     * @param args Argumentos da linha de comando (não utilizados nesta aplicação).
     */
    public static void main(String[] args) {
        System.out.println("""

                        ..+----------------+
                        ./                /|
                        +----------------+  |
                        |  X       X     |   ----+
                        |                 |     /|
                        |  X       X       ----+ |
                        |                      | +
                        |  X       X       X   |/
                        +----------------------+

                """);

        System.out.println("""
                ========================================
                SMART FOUNDRY
                Da areia ao algoritmo.
                ========================================
                Bem-vindo à SMART FOUNDRY!

                Nossa linha de produção transforma wafers
                de silício em processadores de alta
                performance, prontos para dar vida a
                qualquer sistema.

                DESENVOLVIDO POR: VICTOR RIMES & YOHAN ROCHA
                ========================================
                """);

        Scanner sc = new Scanner(System.in);
        PersistenciaPlanta persistencia = new PersistenciaPlanta(ARQUIVO_ESTADO);
        GerenciadorProducao gerenciador = iniciarPlanta(sc, persistencia);

        while (true) {
            exibirCabecalho(gerenciador);
            System.out.println("""
                    1 - Demandas
                    2 - Fabricação
                    3 - Consultar
                    4 - Comprar matéria-prima
                    5 - Gerenciar estratégia
                    6 - Auditoria e manutenção
                    7 - Linha de produção
                    8 - Salvar estado da planta
                    9 - Carregar estado da planta
                    0 - Sair
                    """);

            int opcao = lerInteiro(sc, "Escolha: ");

            switch (opcao) {
                case 1 -> menuDemandas(sc, gerenciador);
                case 2 -> menuFabricacao(sc, gerenciador);
                case 3 -> menuConsultar(sc, gerenciador);
                case 4 -> {
                    int qtd = lerInteiro(sc, "Quantidade de silício (mm2) a comprar: ");
                    
                    if (qtd <= 0) {
                        System.out.println("Operação inválida: A quantidade de compra deve ser superior a zero.");
                        break;
                    }
                    try {
                        gerenciador.comprarMateriaPrima(qtd);
                    } catch (RecursoInsuficienteException e) {
                        System.out.println("ERRO ===\nNão foi possível concluir a compra: " + e.getMessage());
                    }
                }
                case 5 -> menuEstrategia(sc, gerenciador);
                case 6 -> menuAuditoria(sc, gerenciador);
                case 7 -> menuLinha(sc, gerenciador);
                case 8 -> salvarPlanta(persistencia, gerenciador);
                case 9 -> gerenciador = carregarPlanta(sc, persistencia, gerenciador);
                case 0 -> {
                    // Sair não salva nem apaga nada: o último estado salvo continua no arquivo
                    System.out.println("Desligando a linha de litografia. Até a próxima, SMART FOUNDRY!");
                    sc.close();
                    return;
                }
                default -> System.out.println("Opção inválida. Selecione uma ação de 0 a 9.");
            }
        }
    }
}