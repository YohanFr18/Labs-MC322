/**
 * Máquina de Processamento responsável pela etapa de Litografia UV dos processadores.
 * Insere um risco cumulativo no die do processador em caso de falha operacional, sem o rejeitar imediatamente.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class EstacaoLitografia extends Maquina {
    
    /**
     * Construtor da Estação de Litografia com custos e taxa de falha pré-definidos.
     */
    public EstacaoLitografia() {
        super("Estacao de Litografia UV", 760, 0.15f, 75.0);
    }

    /**
     * Executa a litografia num produto.
     * Caso a máquina registe uma falha de operação baseada na sua saúde, o produto não é rejeitado,
     * mas a sua probabilidade de falha acumulada (risco) aumenta.
     *
     * @param produto Produto a ser submetido à litografia.
     * @return {@code true} se o ciclo terminar sem erros de estado, {@code false} se a máquina estiver desligada ou o produto não permitir processamento.
     */
    @Override
    public boolean processar(Produto produto) {
        if (!estaLigada()) {
            return false;
        }
        if (!produto.processar()) {
            return false;
        }
        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha(getProbabilidadeFalha());
        }
        return true;
    }

    @Override
    public String getTipo() {
        return "Processamento";
    }
}