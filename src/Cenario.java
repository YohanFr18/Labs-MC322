public enum Cenario {
    IDEAL("Ideal", 8000.0, 0.6f, 3f),
    APOCALIPTICO("Apocalíptico", 3000.0, 1.6f, 9f);

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
