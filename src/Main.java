import java.util.Scanner;

public class Main {
    static int lerInteiro(Scanner sc, String msg) {
        System.out.print(msg);
        while (!sc.hasNextInt()) {
            System.out.println("Digite apenas números.");
            sc.next();
            System.out.print(msg);
        }
        return sc.nextInt();
    }

    public static void main(String[] args) {
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

        MateriaPrima wafer = new MateriaPrima("SIW-001", "Wafer de Silício", 5000, "mm2", 100);
        Produto turing = new Produto("CPU-001", "Turing-X4", 120);
        Produto lovelace = new Produto("CPU-002", "Lovelace-X8", 320);
        Produto torvalds = new Produto("CPU-003", "Torvalds-X16", 640);

        Maquina maquina = new Maquina("WAFER X-PRESS 2000", 760);
        Esteira esteira = new Esteira(700);
        EstacaoInspecao estacaoInspecao = new EstacaoInspecao();

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println(
                    """
                            ========================================
                            MENU PRINCIPAL - SMART FOUNDRY
                            ========================================
                            1 - Iniciar produção
                            2 - Consultar estoque
                            3 - Adicionar matéria-prima ao estoque
                            4 - Sair
                            """);

            int opcao = lerInteiro(sc, "Escolha: ");
            if (opcao == 1) {
                System.out.println("""
                                1 - Turing-X4    (demanda: 120 mm²)
                                2 - Lovelace-X8  (demanda: 320 mm²)
                                3 - Torvalds-X16 (demanda: 640 mm²)
                        """);

                // Escolha do modelo a ser produzido
                int escolha = lerInteiro(sc, "Modelo (1-3): ");
                Produto produto;
                if (escolha == 1) {
                    produto = turing;
                } else if (escolha == 2) {
                    produto = lovelace;
                } else if (escolha == 3) {
                    produto = torvalds;
                } else {
                    System.out.println("Modelo inválido.");
                    continue;
                }

                int demanda = lerInteiro(sc, "Demanda de wafer (mm2): ");
                // Verificação de estoque da fábrica (insuficiente ou abaixo do limite)
                if (!wafer.verificarDisponibilidade(demanda)) {
                    System.out.println("Estoque insuficiente ou abaixo do mínimo de segurança.");
                    continue;
                }

                // Ligando a esteira, a máquina e ativando a estação de inspeção
                esteira.ligar();
                maquina.ligar();
                estacaoInspecao.ativar();

                // Etapa de transporte da matéria-prima, pela esteira, até a máquina
                esteira.adicionarItem(wafer, demanda);
                MateriaPrima mp = (MateriaPrima) esteira.removerItem();

                // Etapa de processamento do produto
                if (!maquina.processar(mp, produto, demanda)) {
                    System.out.println("Falha no processamento.");
                    continue;
                }

                // Etapa de transporte do produto, pela esteira, até a estação de inspeção
                esteira.adicionarItem(produto, demanda);
                Produto p = (Produto) esteira.removerItem();

                // Etapa de inspeção do produto
                if (!estacaoInspecao.inspecionar(p)) {
                    System.out.println("Falha na inspeção.");
                    continue;
                }

                // Finalizado, produção concluída com sucesso
                System.out.println(p.getNome() + " aprovado. Origem: " + p.getMateriaPrima().getId());
                System.out.println("Estoque restante: " + wafer.getQuantidade() + " " + wafer.getUnidade());

            } else if (opcao == 2) {
                System.out.println(wafer.getNome() + ": " + wafer.getQuantidade() + " " + wafer.getUnidade());
            } else if (opcao == 3) {
                int add_mp = lerInteiro(sc, "Quantidade a adicionar: ");
                wafer.adicionarEstoque(add_mp);

            } else if (opcao == 4) {
                break;
            } else {
                System.out.println("Opção inválida.");
            }
        }
        sc.close();
    }
}
