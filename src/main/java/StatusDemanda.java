/**
 * Representa os estados possíveis do ciclo de vida de uma Demanda na fábrica.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public enum StatusDemanda {
    /** Aguardando produção; é o único status que as estratégias podem selecionar. */
    PENDENTE("Pendente"),
    /** Uma unidade da demanda está passando pela linha neste momento. */
    EM_PRODUCAO("Produzindo"),
    /** Todas as unidades encomendadas foram fabricadas. */
    CONCLUIDA("Concluída"),
    /** Cancelada por falta de wafer ou budget; volta a PENDENTE se a quantidade for atualizada. */
    CANCELADA("Cancelada");

    private final String descricao;

    private StatusDemanda(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna o nome do status exibido na listagem de demandas.
     *
     * @return String formatada para apresentação do status na interface.
     */
    public String getDescricao() {
        return descricao;
    }
}