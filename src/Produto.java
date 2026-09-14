public abstract class Produto {
    private String id;
    private String nome;
    private StatusProduto status;
    private int quantidadeMateriaPrimaPorUnidade;
    private float qualidade;
    private float probabilidadeFalhaAcumulada;
    private static int totalProdutosFabricados;

    public Produto(
            String nome, int quantidadeMateriaPrimaPorUnidade, float qualidade) {
        Produto.totalProdutosFabricados += 1;
        this.id = "CPU-" + totalProdutosFabricados;
        this.nome = nome;
        this.status = StatusProduto.AGUARDANDO_PROCESSAMENTO;
        this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
        this.qualidade = qualidade;
    }

    public abstract boolean processar();

    public abstract int calcularTempoProducao();

    public abstract String getTipo();

    public void aumentarProbabilidadeFalha(float probabilidadeFalha) {
        probabilidadeFalhaAcumulada += probabilidadeFalha;
        if (probabilidadeFalhaAcumulada >= 1.0f) {
            probabilidadeFalhaAcumulada = 1.0f;
        }
    }

    public int getQuantidadeMateriaPrimaPorUnidade() {
        return quantidadeMateriaPrimaPorUnidade;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public StatusProduto getStatus() {
        return status;
    }

    public float getQualidade() {
        return qualidade;
    }

    public void setStatus(StatusProduto status) {
        this.status = status;
    }
}
