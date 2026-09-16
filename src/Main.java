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

        // Construtor atualizado da Tarefa 2: (id, nome, quantidade, unidade, quantidadeMinima, custoPorUnidade)
        MateriaPrima wafer = new MateriaPrima("SIW-001", "Wafer de Silício", 5000, "mm2", 100, 10);
        double budgetInicial = 5000.0;

        // Gerenciador de Produção central da Tarefa 2
        GerenciadorProducao gerenciador = new GerenciadorProducao(wafer, budgetInicial);

        // Registro das demandas dos processadores
        gerenciador.registrarDemanda(new Demanda("Turing-X4", 0));
        gerenciador.registrarDemanda(new Demanda("Lovelace-X8", 0));
        gerenciador.registrarDemanda(new Demanda("Torvalds-X16", 0));

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("""
                    ========================================
                    MENU PRINCIPAL - SMART FOUNDRY
                    ========================================""");
            gerenciador.exibirBudget();
            System.out.println("""
                    1 - Atualizar demandas
                    2 - Fabricar produto
                    3 - Consultar estoque e armazém
                    4 - Comprar matéria-prima
                    5 - Sair
                    """);

            int opcao = lerInteiro(sc, "Escolha: ");

            if (opcao == 1) {
                System.out.println("""
                        1 - Turing-X4
                        2 - Lovelace-X8
                        3 - Torvalds-X16
                        """);
                int escolha = lerInteiro(sc, "Selecione o modelo (1-3): ");
                String modelo = switch (escolha) {
                    case 1 -> "Turing-X4";
                    case 2 -> "Lovelace-X8";
                    case 3 -> "Torvalds-X16";
                    default -> null;
                };

                if (modelo == null) {
                    System.out.println("Modelo inválido.");
                    continue;
                }

                int novaQtd = lerInteiro(sc, "Nova quantidade da demanda: ");
                gerenciador.atualizarDemanda(modelo, novaQtd);

            } else if (opcao == 2) {
                System.out.println("""
                        1 - Turing-X4
                        2 - Lovelace-X8
                        3 - Torvalds-X16
                        """);
                int escolha = lerInteiro(sc, "Selecione o modelo (1-3): ");
                String modelo = switch (escolha) {
                    case 1 -> "Turing-X4";
                    case 2 -> "Lovelace-X8";
                    case 3 -> "Torvalds-X16";
                    default -> null;
                };

                if (modelo == null) {
                    System.out.println("Modelo inválido.");
                    continue;
                }

                gerenciador.fabricarDemanda(modelo);

            } else if (opcao == 3) {
                System.out.println("--- ESTOQUE DE MATÉRIA-PRIMA ---");
                System.out.println(wafer.getNome() + ": " + wafer.getQuantidade() + " " + wafer.getUnidade());
                System.out.println("\n--- ARMAZÉM DE PRODUTOS ---");
                gerenciador.exibirArmazem();

            } else if (opcao == 4) {
                int qtd = lerInteiro(sc, "Quantidade de silício (mm2) a comprar: ");
                gerenciador.comprarMateriaPrima(qtd);

            } else if (opcao == 5) {
                System.out.println("Fechando a fábrica.");
                break;
            } else {
                System.out.println("Opção inválida.");
            }
        }
        sc.close();
    }
}