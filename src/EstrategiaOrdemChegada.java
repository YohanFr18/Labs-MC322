import java.util.List;

public class EstrategiaOrdemChegada implements EstrategiaProducao {

    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        if (demandas == null || demandas.isEmpty()) {
            return null;
        }

        for (Demanda demanda : demandas) {
            // Seleciona a primeira demanda da fila que esteja PENDENTE e com itens a fabricar
            if (demanda.getStatus() == StatusDemanda.PENDENTE && demanda.getQuantidadeProdutos() > 0) {
                return demanda;
            }
        }

        return null;
    }

    @Override
    public String getNomeEstrategia() {
        return "Ordem de Chegada (FIFO)";
    }
}