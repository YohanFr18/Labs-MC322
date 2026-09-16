public class EstacaoMetrologia extends Maquina {
    public EstacaoMetrologia() {
        super("Estacao de Metrologia", 760, 0.1f, 100.0);
    }

    @Override
    public boolean processar(Produto produto) {
        if (!estaLigada()) {
            return false;
        }

        if (produto.getStatus() != StatusProduto.PROCESSADO) {
            return false;
        }

        float probabilidadeRejeicao = (getProbabilidadeFalha() + produto.getQualidade()
                + produto.getProbabilidadeFalhaAcumulada()) / 3;
        if (verificarFalha(probabilidadeRejeicao)) {
            produto.setStatus(StatusProduto.REJEITADO);
        } else {
            produto.setStatus(StatusProduto.INSPECIONADO);
        }
        return true;
    }

    @Override
    public String getTipo() {
        return "Inspeção";
    }
}
