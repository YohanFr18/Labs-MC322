/**
 * Máquina de Inspeção responsável pela avaliação final de qualidade dos produtos processados.
 * Capaz de aprovar ou rejeitar componentes com base num cálculo de tolerância estrito.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class EstacaoMetrologia extends Maquina {
    
    /**
     * Construtor da Estação de Metrologia com custos e taxa de falha pré-definidos.
     */
    public EstacaoMetrologia() {
        super("Estacao de Metrologia", 760, 0.1f, 100.0);
    }

    /**
     * Executa a inspeção de um produto acabado para definir o seu status final.
     * A possibilidade de rejeição é uma média entre a falha atual da máquina,
     * o grau de qualidade exigido pelo modelo de chip e os riscos que acumulou nas máquinas anteriores.
     *
     * @param produto Produto finalizado nas fases anteriores da linha de produção.
     * @return {@code true} se a inspeção ocorreu, {@code false} se a máquina estiver desligada ou o produto não estiver no estado PROCESSADO.
     */
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