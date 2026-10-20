import java.util.Scanner;

/**
 * Classe principal que contém o ponto de entrada da aplicação e o loop de menus interativos da Smart Foundry.
 * É responsável por interagir com o usuário, validar a entrada de dados (evitando falhas por tipos incorretos) 
 * e tratar adequadamente as exceções de domínio geradas pelo sistema.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class Main {

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
                    System.out.println("Realize a manutenção da máquina na seção de Auditoria.");
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
     * @param wafer Instância da matéria-prima utilizada na fábrica.
     */
    static void menuConsultar(Scanner sc, GerenciadorProducao gerenciador, MateriaPrima wafer) {
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
            System.out.println(" 2 - Detalhar máquinas");
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
                gerenciador.detalharMaquinas();
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
        Cenario cenario = escolherCenario(sc);
        MateriaPrima wafer = new MateriaPrima("SIW-001", "Wafer de Silício", 5000, "mm2", 100, 10);
        GerenciadorProducao gerenciador = new GerenciadorProducao(wafer, cenario);
        
        gerenciador.setEstrategia(new EstrategiaFilaDeWafers());
        gerenciador.registrarDemanda(new Demanda("Turing-X4", 0));
        gerenciador.registrarDemanda(new Demanda("Lovelace-X8", 0));
        gerenciador.registrarDemanda(new Demanda("Torvalds-X16", 0));

        while (true) {
            exibirCabecalho(gerenciador);
            System.out.println("""
                    1 - Demandas
                    2 - Fabricação
                    3 - Consultar
                    4 - Comprar matéria-prima
                    5 - Gerenciar estratégia
                    6 - Auditoria e manutenção
                    0 - Sair
                    """);

            int opcao = lerInteiro(sc, "Escolha: ");

            switch (opcao) {
                case 1 -> menuDemandas(sc, gerenciador);
                case 2 -> menuFabricacao(sc, gerenciador);
                case 3 -> menuConsultar(sc, gerenciador, wafer);
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
                case 0 -> {
                    System.out.println("Desligando a linha de litografia. Até a próxima, SMART FOUNDRY!");
                    sc.close();
                    return;
                }
                default -> System.out.println("Opção inválida. Selecione uma ação de 0 a 6.");
            }
        }
    }
}