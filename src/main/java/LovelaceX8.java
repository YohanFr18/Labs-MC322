/**
 * Processador Lovelace-X8, o modelo intermediário (média qualidade) da SMART FOUNDRY.
 * Consome 320 mm2 de wafer por unidade, tem qualidade 0,7 e tempo base de
 * produção de 80 ciclos.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class LovelaceX8 extends Produto {
    /**
     * Cria uma unidade do Lovelace-X8, aguardando processamento.
     */
    public LovelaceX8() {
        super("Lovelace-X8", 320, 0.7f);
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
        int tempoBase = 80;
        return tempoBase + (getQuantidadeMateriaPrimaPorUnidade() / 10) + (int) (getQualidade() * 30);
    }

    @Override
    public String getTipo() {
        return "Processador de Média Qualidade";
    }
}
