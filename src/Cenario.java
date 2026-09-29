// Cada cenário define o budget inicial e os multiplicadores de falha e desgaste das máquinas
// valores abaixo de 1 aliviam a operação e valores acima de 1 a tornam mais agressiva
public enum Cenario {
    IDEAL("Ideal", 8000.0, 0.6f, 0.5f),
    APOCALIPTICO("Apocalíptico", 3000.0, 1.6f, 2.0f);

    private final String descricao;
    private final double budgetInicial;
    private final float multiplicadorFalha;
    private final float multiplicadorDesgaste;

    private Cenario(String descricao, double budgetInicial, float multiplicadorFalha, float multiplicadorDesgaste) {
        this.descricao = descricao;
        this.budgetInicial = budgetInicial;
        this.multiplicadorFalha = multiplicadorFalha;
        this.multiplicadorDesgaste = multiplicadorDesgaste;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getBudgetInicial() {
        return budgetInicial;
    }

    public float getMultiplicadorFalha() {
        return multiplicadorFalha;
    }

    public float getMultiplicadorDesgaste() {
        return multiplicadorDesgaste;
    }
}
