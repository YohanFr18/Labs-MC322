public class TuringX4 extends Produto {
    public TuringX4() {
        super("Turing-X4", 120, 0.5f);
    }

    @Override
    public boolean processar() {
        if (getStatus() != StatusProduto.AGUARDANDO_PROCESSAMENTO) {
            return false;
        }

        setStatus(StatusProduto.PROCESSADO);
        return true;
    }

    @Override
    public int calcularTempoProducao() {
        int tempoBase = 30;
        return tempoBase + (getQuantidadeMateriaPrimaPorUnidade() / 10) + (int) (getQualidade() * 30);
    }

    @Override
    public String getTipo() {
        return "Processador de Baixa Qualidade";
    }

}
