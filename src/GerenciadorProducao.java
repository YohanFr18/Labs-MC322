import java.util.ArrayList;

public class GerenciadorProducao {
    private ArrayList<Demanda> demandas;
    private ArrayList<Produto> produtosFabricados;
    private ArrayList<Maquina> maquinas;
    private MateriaPrima materiaPrima;
    private double budget;

    public GerenciadorProducao(MateriaPrima materiaPrima, double budget) {
        this.materiaPrima = materiaPrima;
        this.budget = budget;
        demandas = new ArrayList<>();
        produtosFabricados = new ArrayList<>();
        maquinas = new ArrayList<>();
        Maquina litografia = new EstacaoLitografia();
        Maquina encapsulamento = new EstacaoEncapsulamento();
        Maquina metrologia = new EstacaoMetrologia();
        litografia.ligar();
        encapsulamento.ligar();
        metrologia.ligar();
        maquinas.add(litografia);
        maquinas.add(encapsulamento);
        maquinas.add(metrologia);
    }

    public void registrarDemanda(Demanda d) {
        demandas.add(d);
    }

    public void atualizarDemanda(String modelo, int novaQuantidade) {
        for (int i = 0; i < demandas.size(); i++) {
            Demanda aux = demandas.get(i);

            if (aux.getTipoProduto().equals(modelo)) {
                aux.atualizarQuantidade(novaQuantidade);
            }
        }
    }

    public void exibirBudget() {
        System.out.printf("BUDGET ATUAL: R$%.2f%n", budget);
    }

    public void exibirArmazem() {
        if (produtosFabricados.isEmpty()) {
            System.out.println("[FAIL] Lista Vazia.");
        } else {
            for (Produto d : produtosFabricados) {
                System.out.println("- " + d.getId() + " " + d.getNome() + " (" + d.getTipo() + ") - " + d.getStatus());
            }
        }
    }

    public void comprarMateriaPrima(int quantidade) {
        int custo = (quantidade * materiaPrima.getCustoPorUnidade());
        if (custo <= budget) {
            budget -= custo;
            materiaPrima.adicionarEstoque(quantidade);
        }
    }

    public void fabricarDemanda(String modelo) {
        for (int i = 0; i < demandas.size(); i++) {
            Demanda aux = demandas.get(i);

            if (aux.getTipoProduto().equals(modelo)) {
                if (aux.getQuantidadeProdutos() <= 0) {
                    return;
                }

                Produto produto = switch (modelo) {
                    case "Turing-X4" -> new TuringX4();
                    case "Lovelace-X8" -> new LovelaceX8();
                    case "Torvalds-X16" -> new TorvaldsX16();
                    default -> null;
                };
                if (produto == null) {
                    return;
                }
                if (!materiaPrima.verificarDisponibilidade(produto.getQuantidadeMateriaPrimaPorUnidade())) {
                    return;
                }
                double custoTotal = calcularCustoProducao();
                if (custoTotal > budget) {
                    return;
                }

                for (Maquina m : maquinas) {
                    if (!m.processar(produto)) {
                        return;
                    }
                }
                budget -= custoTotal;
                materiaPrima.consumir(produto.getQuantidadeMateriaPrimaPorUnidade());
                produtosFabricados.add(produto);
                aux.atualizarQuantidade(aux.getQuantidadeProdutos() - 1);
                if (aux.getQuantidadeProdutos() == 0) {
                    aux.atender();
                }
            }

        }

    }

    private double calcularCustoProducao() {
        double total = 0;
        for (Maquina m : maquinas) {
            total += m.getCustoOperacao();
        }
        return total;
    }
}
