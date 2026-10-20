/**
 * Enumeração que representa os diferentes cenários de dificuldade de operação da fábrica.
 * Define o orçamento inicial e os multiplicadores de falha e desgaste das máquinas.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public enum Cenario {
    /** Mercado estável: budget de R$ 50000 e máquinas com falha e desgaste normais. */
    IDEAL("Cenário Ideal", 50000.0, 1.0f, 1.0),
    /** Crise do silício: budget de R$ 15000, falhas 2,5x mais prováveis e desgaste 3x mais rápido. */
    APOCALIPTICO("Crise do Silício", 15000.0, 2.5f, 3.0);

    private final String descricao;
    private final double budgetInicial;
    private final float multiplicadorFalha;
    private final double multiplicadorDesgaste;

    private Cenario(String descricao, double budgetInicial, float multiplicadorFalha, double multiplicadorDesgaste) {
        this.descricao = descricao;
        this.budgetInicial = budgetInicial;
        this.multiplicadorFalha = multiplicadorFalha;
        this.multiplicadorDesgaste = multiplicadorDesgaste;
    }

    /**
     * Retorna o nome do cenário exibido no cabeçalho do menu.
     *
     * @return Descrição do cenário para a interface.
     */
    public String getDescricao() { return descricao; }

    /**
     * Retorna o budget com que a fábrica começa neste cenário.
     *
     * @return Valor financeiro inicial fornecido à fábrica.
     */
    public double getBudgetInicial() { return budgetInicial; }

    /**
     * Retorna o multiplicador aplicado à probabilidade de falha de todas as máquinas.
     *
     * @return Fator que agrava a probabilidade de erro das máquinas.
     */
    public float getMultiplicadorFalha() { return multiplicadorFalha; }

    /**
     * Retorna o multiplicador aplicado ao desgaste sofrido pelas máquinas a cada uso.
     *
     * @return Fator que acelera a degradação da saúde mecânica.
     */
    public double getMultiplicadorDesgaste() { return multiplicadorDesgaste; }
}