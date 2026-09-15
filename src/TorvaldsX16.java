public class TorvaldsX16 extends Produto {
    public TorvaldsX16() {
        super("Torvalds-X16", 640, 0.9f);
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
        int tempoBase = 160;
        return tempoBase + (getQuantidadeMateriaPrimaPorUnidade() / 10) + (int) (getQualidade() * 30);
    }

    @Override
    public String getTipo() {
        return "Processador de Alta Qualidade";
    }
}
