import java.util.Scanner;

public class Main {
    static int lerInteiro(Scanner sc, String msg) {
        System.out.print(msg);
        while (!sc.hasNextInt()) {
            if (!sc.hasNext()) { // entrada encerrada (EOF / Ctrl+D)
                System.out.println("\nEntrada encerrada. Fechando a fábrica.");
                System.exit(0);
            }
            System.out.println("Digite apenas números.");
            sc.next();
            System.out.print(msg);
        }
        return sc.nextInt();
    }

    static void imprimirSecao(String titulo) {
        System.out.println("--------------------------------------------------------------");
        System.out.println(" [" + titulo + "]");
        System.out.println("--------------------------------------------------------------");
    }

    static void exibirCabecalho(GerenciadorProducao gerenciador) {
        System.out.println("==============================================================");
        System.out.println("  SMART FOUNDRY - Da areia ao algoritmo");
        System.out.println("--------------------------------------------------------------");
        System.out.println("  ESTRATÉGIA ATUAL : " + gerenciador.getNomeEstrategiaAtual());
        System.out.println("  CENÁRIO ATIVO    : " + gerenciador.getNomeCenarioAtivo());
        System.out.printf("  BUDGET ATUAL     : R$ %.2f%n", gerenciador.getBudget());
        System.out.println("==============================================================");
    }

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
            System.out.println("Cenário inválido.");
        }
    }

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
                    System.out.println("Modelo inválido.");
                    continue;
                }
                int novaQtd = lerInteiro(sc, "Nova quantidade da demanda: ");
                gerenciador.atualizarDemanda(modelo, novaQtd);
            } else if (opcao == 2) {
                gerenciador.listarDemandas();
            } else {
                System.out.println("Opção inválida.");
            }
        }
    }

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
            } else if (opcao == 1) {
                gerenciador.executarProximaProducao();
            } else if (opcao == 2) {
                String modelo = escolherModelo(sc);
                if (modelo == null) {
                    System.out.println("Modelo inválido.");
                    continue;
                }
                gerenciador.fabricarDemanda(modelo);
            } else {
                System.out.println("Opção inválida.");
            }
        }
    }

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
                System.out.println("Opção inválida.");
            }
            return;
        }
        gerenciador.setEstrategia(novaEstrategia);
        System.out.println("Estratégia alterada para: " + novaEstrategia.getNomeEstrategia());
    }

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
                gerenciador.realizarManutencao();
            } else {
                System.out.println("Opção inválida.");
            }
        }
    }

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

        // (id, nome, quantidade, unidade, quantidadeMinima, custoPorUnidade)
        MateriaPrima wafer = new MateriaPrima("SIW-001", "Wafer de Silício", 5000, "mm2", 100, 10);

        GerenciadorProducao gerenciador = new GerenciadorProducao(wafer, cenario);
        // A fábrica começa na Fila de Wafers, e a estratégia pode ser trocada pelo menu
        gerenciador.setEstrategia(new EstrategiaFilaDeWafers());

        // As demandas começam zeradas, o usuário define as quantidades no menu Demandas
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
                    gerenciador.comprarMateriaPrima(qtd);
                }
                case 5 -> menuEstrategia(sc, gerenciador);
                case 6 -> menuAuditoria(sc, gerenciador);
                case 0 -> {
                    System.out.println("Desligando a linha de litografia. Até a próxima, SMART FOUNDRY!");
                    sc.close();
                    return;
                }
                default -> System.out.println("Opção inválida.");
            }
        }
    }
}
