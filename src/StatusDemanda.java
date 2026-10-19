/**
 * Representa os estados possíveis do ciclo de vida de uma Demanda na fábrica.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public enum StatusDemanda {
    PENDENTE("Pendente"),
    EM_PRODUCAO("Produzindo"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada");

    private final String descricao;

    private StatusDemanda(String descricao) {
        this.descricao = descricao;
    }

    /** @return String formatada para apresentação do status na interface. */
    public String getDescricao() {
        return descricao;
    }
}