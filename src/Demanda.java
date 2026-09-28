public class Demanda {
    private String tipoProduto;
    private int quantidadeProdutos;
    private StatusDemanda status;

    public Demanda(String tipoProduto, int quantidadeProdutos) {
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = Math.max(0, quantidadeProdutos);
        this.status = StatusDemanda.PENDENTE;
    }

    public boolean atualizarQuantidade(int novaQuantidade) {
        if (novaQuantidade < 0) {
            return false;
        }

        this.quantidadeProdutos = novaQuantidade;

        if (novaQuantidade > 0) {
            this.status = StatusDemanda.PENDENTE;
        }

        return true;
    }

    public int calcularMateriaPrimaNecessaria(int quantidadePorUnidade) {
        if (quantidadePorUnidade <= 0 || quantidadeProdutos <= 0) {
            return 0;
        }
        return this.quantidadeProdutos * quantidadePorUnidade;
    }

    public void cancelar() {
        if (this.status != StatusDemanda.CONCLUIDA) {
            this.status = StatusDemanda.CANCELADA;
        }
    }

    public void atender() {
        if (this.status != StatusDemanda.CANCELADA) {
            this.status = StatusDemanda.CONCLUIDA;
        }
    }

    public String getTipoProduto() {
        return tipoProduto;
    }

    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    public StatusDemanda getStatus() {
        return status;
    }
}