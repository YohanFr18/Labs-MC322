public class EstacaoInspecao {
    // Atributos
    private boolean ativa;
    private int produtosInspecionados;

    // Construtor
    public EstacaoInspecao() {
        ativa = false;
        produtosInspecionados = 0;
    }

    // Ativa a estação
    public void ativar() {
        ativa = true;
    }

    // Desativa a estação
    public void desativar() {
        ativa = false;
    }

    public boolean inspecionar(String produto) {
        if (!ativa) {
            return false;
        }

        if (produto == null) {
            return false;
        }

        produtosInspecionados++;
        return true;
    }

    public int getTotalInspecionados() {
        return produtosInspecionados;
    }

    public boolean isAtiva() {
        return ativa;
    }
}