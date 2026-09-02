import java.util.Scanner;

public class Main {
    static int lerInteiro(Scanner sc, String msg) {
        System.out.print(msg);
        while(!sc.hasNextInt()) {
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
                System.out.println("produzir");
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
