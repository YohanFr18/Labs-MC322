import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GerenciadorProducao {
    private ArrayList<Demanda> demandas;
    private ArrayList<Produto> produtosFabricados;
    private ArrayList<Maquina> maquinas;
    private MateriaPrima materiaPrima;
    private double budget;
    private Cenario cenarioAtivo;

    public GerenciadorProducao(MateriaPrima materiaPrima, Cenario cenario) {
        this.materiaPrima = materiaPrima;
        this.cenarioAtivo = cenario;
        this.budget = cenario.getBudgetInicial();
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

    public String getNomeCenarioAtivo() {
        return cenarioAtivo.getDescricao();
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
            return;
        }

        Map<String, List<Produto>> lotes = new LinkedHashMap<>();
        for (Produto p : produtosFabricados) {
            lotes.computeIfAbsent(p.getTipo(), k -> new ArrayList<>()).add(p);
        }

        for (Map.Entry<String, List<Produto>> lote : lotes.entrySet()) {
            List<Produto> produtosDoLote = lote.getValue();
            float qualidadeMedia = 0;
            float riscoMedio = 0;
            for (Produto p : produtosDoLote) {
                qualidadeMedia += p.getQualidade();
                riscoMedio += p.getProbabilidadeFalhaAcumulada();
            }
            qualidadeMedia /= produtosDoLote.size();
            riscoMedio /= produtosDoLote.size();
            System.out.printf("- %s | Qtd: %d | Qualidade média: %.2f | Risco médio: %.2f%n",
                    lote.getKey(), produtosDoLote.size(), qualidadeMedia, riscoMedio);
        }
    }

    public void comprarMateriaPrima(int quantidade) {
        if (quantidade <= 0) {
            return;
        }
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
