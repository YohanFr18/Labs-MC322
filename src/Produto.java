public abstract class Produto {
    private String id;
    private String nome;
    private StatusProduto status;
    private int quantidadeMateriaPrimaNecessaria;
    private float qualidade;
    private float probabilidadeFalhaAcumulada;
    private static int totalProdutosFabricados;

    public Produto(
            String nome, int quantidadeMateriaPrimaNecessaria, float qualidade) {
        this.totalProdutosFabricados += 1;
        this.id = "CPU-" + totalProdutosFabricados;
        this.nome = nome;
        this.status = StatusProduto.AGUARDANDO_PROCESSAMENTO;
        this.quantidadeMateriaPrimaNecessaria = quantidadeMateriaPrimaNecessaria;
        this.qualidade = qualidade;
    }

    public abstract boolean processar();

    public abstract int calcularTempoProducao();

    public abstract String getTipo();

    public boolean inspecionar() {
        if (status != StatusProduto.PROCESSADO) {
            return false;
        }
        status = StatusProduto.INSPECIONADO;

        return true;
    }

    public int getDemandaMateriaPrima() {
        return quantidadeMateriaPrimaNecessaria;
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
}
