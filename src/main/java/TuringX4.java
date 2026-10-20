/**
 * Processador Turing-X4, o modelo de entrada (baixa qualidade) da SMART FOUNDRY.
 * Consome 120 mm2 de wafer por unidade, tem qualidade 0,5 e tempo base de
 * produção de 30 ciclos.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class TuringX4 extends Produto {
    /**
     * Cria uma unidade do Turing-X4, aguardando processamento.
     */
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
