import java.util.List;

/**
 * Estratégia de produção baseada em ordem de chegada (FIFO).
 * Prioriza sempre a primeira demanda pendente da lista que possua unidades a fabricar.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class EstrategiaFilaDeWafers implements EstrategiaProducao {

    /**
     * Cria a estratégia Fila de Wafers. Ela não guarda estado entre as seleções.
     */
    public EstrategiaFilaDeWafers() {
    }

    /**
     * Seleciona a primeira demanda da lista que esteja no estado PENDENTE.
     * Não tem em conta o orçamento, a viabilidade ou a dimensão da encomenda.
     *
     * @param demandas Lista de demandas registradas no sistema.
     * @param orcamentoDisponivel Orçamento da fábrica (não utilizado nesta estratégia).
     * @return A primeira {@link Demanda} pendente, ou {@code null} se não houver demandas válidas.
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        if (demandas == null || demandas.isEmpty()) {
            return null;
        }

        for (Demanda demanda : demandas) {
            if (demanda.getStatus() == StatusDemanda.PENDENTE && demanda.getQuantidadeProdutos() > 0) {
                return demanda;
            }
        }
        return null;
    }

    @Override
    public String getNomeEstrategia() {
        return "Fila de Wafers (ordem de chegada)";
    }
}