/**
 * Classe abstrata que representa um processador físico a ser manufaturado.
 * Implementa a interface Auditavel para permitir o rastreio do seu risco estrutural.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public abstract class Produto implements Auditavel {
    private String id;
    private String nome;
    private StatusProduto status;
    private int quantidadeMateriaPrimaPorUnidade;
    private float qualidade;
    private float probabilidadeFalhaAcumulada;
    private static int totalProdutosFabricados;

    /**
     * Construtor base para os produtos da fábrica.
     * Atribui automaticamente um ID sequencial à unidade.
     *
     * @param nome Nome do modelo do processador.
     * @param quantidadeMateriaPrimaPorUnidade Milímetros quadrados de wafer de silício necessários.
     * @param qualidade Padrão de qualidade esperado do modelo (afeta aprovação metrológica).
     */
    public Produto(String nome, int quantidadeMateriaPrimaPorUnidade, float qualidade) {
        Produto.totalProdutosFabricados += 1;
        this.id = "CPU-" + totalProdutosFabricados;
        this.nome = nome;
        this.status = StatusProduto.AGUARDANDO_PROCESSAMENTO;
        this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
        this.qualidade = qualidade;
    }

    @Override
    public boolean precisaManutencao() {
        return probabilidadeFalhaAcumulada > 0.5;
    }

    @Override
    public String gerarRelatorioDiagnostico() {
        return id + " - Qualidade: " + qualidade + " - Risco acumulado: " + probabilidadeFalhaAcumulada;
    }

    /**
     * Tenta avançar o produto para a próxima fase do processamento.
     *
     * @return {@code true} se o estado foi atualizado com sucesso, {@code false} caso contrário.
     */
    public abstract boolean processar();

    /** @return O tempo estimado em ciclos para concluir este modelo. */
    public abstract int calcularTempoProducao();

    /** @return A classificação da gama deste modelo (ex: Alta Qualidade). */
    public abstract String getTipo();

    /**
     * Aumenta a probabilidade do produto ser rejeitado na metrologia após falhas nas máquinas.
     * O valor não excede 1.0f (100% de risco).
     *
     * @param probabilidadeFalha O risco percentual a ser adicionado.
     */
    public void aumentarProbabilidadeFalha(float probabilidadeFalha) {
        probabilidadeFalhaAcumulada += probabilidadeFalha;
        if (probabilidadeFalhaAcumulada >= 1.0f) {
            probabilidadeFalhaAcumulada = 1.0f;
        }
    }

    public int getQuantidadeMateriaPrimaPorUnidade() { return quantidadeMateriaPrimaPorUnidade; }
    public String getId() { return id; }
    public String getNome() { return nome; }
    public StatusProduto getStatus() { return status; }
    public float getQualidade() { return qualidade; }
    public void setStatus(StatusProduto status) { this.status = status; }
    public float getProbabilidadeFalhaAcumulada() { return probabilidadeFalhaAcumulada; }
}