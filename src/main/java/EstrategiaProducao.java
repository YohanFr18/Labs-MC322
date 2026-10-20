import java.util.List;

/**
 * Interface que define o contrato para as estratégias de seleção de demandas na linha de produção.
 * Permite a implementação de diferentes regras de negócio para decidir qual produto fabricar em seguida.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public interface EstrategiaProducao {
    
    /**
     * Examina a lista de demandas disponíveis e seleciona a mais adequada segundo a estratégia atual.
     *
     * @param demandas Lista atual de demandas registradas no sistema.
     * @param orcamentoDisponivel Orçamento atual da fábrica para validar a viabilidade de produção.
     * @return A referência para a {@link Demanda} selecionada, ou {@code null} caso nenhuma seja elegível.
     */
    Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);

    /**
     * Obtém o nome descritivo da estratégia em uso.
     *
     * @return String com o nome da estratégia para exibição na interface com o usuário.
     */
    String getNomeEstrategia();
}