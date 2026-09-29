import java.util.List;

public class EstrategiaMaximoProdutos implements EstrategiaProducao {

    // Valores padrão de referência para insumo/custo unitário caso não venham do produto/estoque
    private static final int CUSTO_INSUMO_PADRAO = 10;
    private static final int INSUMO_POR_UNIDADE_PADRAO = 1;

    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        if (demandas == null || demandas.isEmpty()) {
            return null;
        }

        Demanda melhorOpcao = null;
        int maxProdutosViaveis = -1;

        for (Demanda demanda : demandas) {
            if (demanda.getStatus() == StatusDemanda.PENDENTE && demanda.getQuantidadeProdutos() > 0) {
                // Valida se a demanda cabe no orçamento disponível
                boolean viavel = demanda.verificarViabilidadeFinanceira(
                    orcamentoDisponivel,
                    CUSTO_INSUMO_PADRAO,
                    INSUMO_POR_UNIDADE_PADRAO
                );

                if (viavel && demanda.getQuantidadeProdutos() > maxProdutosViaveis) {
                    maxProdutosViaveis = demanda.getQuantidadeProdutos();
                    melhorOpcao = demanda;
                }
            }
        }

        return melhorOpcao;
    }

    @Override
    public String getNomeEstrategia() {
        return "Maximizar Produtos Produzidos (Respeitando Budget)";
    }
}