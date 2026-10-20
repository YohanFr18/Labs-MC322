import java.util.List;

/**
 * Estratégia de produção que prioriza encomendas de grande volume.
 * Seleciona sempre a demanda pendente que tenha a maior quantidade de unidades a fabricar.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class EstrategiaLoteMaximo implements EstrategiaProducao {

    /**
     * Cria a estratégia Lote Máximo. Ela não guarda estado entre as seleções.
     */
    public EstrategiaLoteMaximo() {
    }

    /**
     * Itera sobre todas as demandas pendentes e identifica aquela com a maior quantidade restante.
     *
     * @param demandas Lista de demandas registradas.
     * @param orcamentoDisponivel Orçamento da fábrica (não utilizado nesta estratégia).
     * @return A {@link Demanda} pendente com maior número de itens, ou {@code null} se não houver demandas válidas.
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        if (demandas == null || demandas.isEmpty()) {
            return null;
        }

        Demanda maiorDemanda = null;
        int maiorQuantidade = -1;

        for (Demanda demanda : demandas) {
            if (demanda.getStatus() == StatusDemanda.PENDENTE && demanda.getQuantidadeProdutos() > 0) {
                if (demanda.getQuantidadeProdutos() > maiorQuantidade) {
                    maiorQuantidade = demanda.getQuantidadeProdutos();
                    maiorDemanda = demanda;
                }
            }
        }
        return maiorDemanda;
    }

    @Override
    public String getNomeEstrategia() {
        return "Lote Máximo (maior encomenda)";
    }
}