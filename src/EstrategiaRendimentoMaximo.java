import java.util.List;

/**
 * Estratégia de produção focada em viabilidade financeira e volume.
 * Seleciona a demanda pendente mais volumosa que ainda seja possível fabricar dentro do orçamento atual.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class EstrategiaRendimentoMaximo implements EstrategiaProducao {

    private static final int CUSTO_INSUMO_PADRAO = 10;
    private static final int INSUMO_POR_UNIDADE_PADRAO = 1;

    /**
     * Seleciona a maior demanda que o orçamento da fábrica consegue suportar atualmente.
     * Realiza uma verificação prévia dos custos de produção.
     *
     * @param demandas Lista de demandas disponíveis.
     * @param orcamentoDisponivel Orçamento disponível na fábrica.
     * @return A maior {@link Demanda} financeiramente viável, ou {@code null} se nenhuma cumprir os requisitos.
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        if (demandas == null || demandas.isEmpty()) {
            return null;
        }

        Demanda melhorOpcao = null;
        int maxProdutosViaveis = -1;

        for (Demanda demanda : demandas) {
            if (demanda.getStatus() == StatusDemanda.PENDENTE && demanda.getQuantidadeProdutos() > 0) {
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
        return "Rendimento Máximo (dentro do budget)";
    }
}