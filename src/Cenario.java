/**
 * Enumeração que representa os diferentes cenários de dificuldade de operação da fábrica.
 * Define o orçamento inicial e os multiplicadores de falha e desgaste das máquinas.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public enum Cenario {
    IDEAL("Cenário Ideal", 50000.0, 1.0f, 1.0),
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

    /** @return Descrição do cenário para a interface. */
    public String getDescricao() { return descricao; }
    /** @return Valor financeiro inicial fornecido à fábrica. */
    public double getBudgetInicial() { return budgetInicial; }
    /** @return Fator que agrava a probabilidade de erro das máquinas. */
    public float getMultiplicadorFalha() { return multiplicadorFalha; }
    /** @return Fator que acelera a degradação da saúde mecânica. */
    public double getMultiplicadorDesgaste() { return multiplicadorDesgaste; }
}