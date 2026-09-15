public class Demanda {
    private String tipoProduto;
    private int quantidadeProdutos;
    private boolean atendida;

    public Demanda(String tipoProduto, int quantidadeProdutos) {
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = Math.max(0, quantidadeProdutos);
        this.atendida = false;
    }

    public boolean atualizarQuantidade(int novaQuantidade) {
        if (novaQuantidade < 0) {
            return false;
        }
        
        this.quantidadeProdutos = novaQuantidade;

        if (novaQuantidade > 0) {
            this.atendida = false;
        }
        return true;
    }

    public int calcularMateriaPrimaNecessaria(int quantidadePorUnidade) {
        if (quantidadePorUnidade <= 0 || quantidadeProdutos <= 0) {
            return 0;
        }
        return this.quantidadeProdutos * quantidadePorUnidade;
    }

    public void atender() {
        this.atendida = true;
    }

    public String getTipoProduto() {
        return tipoProduto;
    }

    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    public boolean isAtendida() {
        return atendida;
    }
}