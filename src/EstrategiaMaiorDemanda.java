import java.util.List;

public class EstrategiaMaiorDemanda implements EstrategiaProducao {

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
        return "Maior Demanda (Lote Máximo)";
    }
}