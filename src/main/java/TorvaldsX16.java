/**
 * Processador Torvalds-X16, o modelo topo de linha (alta qualidade) da SMART FOUNDRY.
 * Consome 640 mm2 de wafer por unidade, tem qualidade 0,9 e tempo base de
 * produção de 160 ciclos.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class TorvaldsX16 extends Produto {
    /**
     * Cria uma unidade do Torvalds-X16, aguardando processamento.
     */
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
