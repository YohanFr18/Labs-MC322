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

        // Uma quantidade positiva reativa a demanda, inclusive se ela tiver sido cancelada
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

    public boolean verificarViabilidadeFinanceira(double orcamentoDisponivel, int custoPorUnidadeMateriaPrima,
            int quantidadeMateriaPrimaPorUnidade) {
        int aux = calcularMateriaPrimaNecessaria(quantidadeMateriaPrimaPorUnidade);
        int custoTotal = aux * custoPorUnidadeMateriaPrima;

        return custoTotal <= orcamentoDisponivel;
    }

    public boolean iniciarProducao() {
        if (this.status != StatusDemanda.PENDENTE || this.quantidadeProdutos <= 0) {
            return false;
        }
        this.status = StatusDemanda.EM_PRODUCAO;
        return true;
    }

    // Enquanto ainda houver unidades, a demanda volta para PENDENTE
    // para que a estratégia consiga selecioná-la no próximo ciclo de produção
    public void registrarUnidadeProduzida() {
        if (this.status != StatusDemanda.EM_PRODUCAO) {
            return;
        }
        this.quantidadeProdutos--;
        if (this.quantidadeProdutos == 0) {
            atender();
        } else {
            this.status = StatusDemanda.PENDENTE;
        }
    }

    public void interromperProducao() {
        if (this.status == StatusDemanda.EM_PRODUCAO) {
            this.status = StatusDemanda.PENDENTE;
        }
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