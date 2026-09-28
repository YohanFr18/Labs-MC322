public enum StatusDemanda {
    PENDENTE("Pendente"),
    EM_PRODUCAO("Produzindo"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada");

    private final String descricao;

    private StatusDemanda(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
