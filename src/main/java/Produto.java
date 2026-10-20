/**
 * Classe abstrata que representa um processador físico a ser manufaturado.
 * Implementa a interface Auditavel para permitir o rastreamento do seu risco estrutural.
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

    /**
     * Calcula o tempo de produção a partir do tempo base do modelo, da matéria-prima e da qualidade.
     *
     * @return O tempo estimado em ciclos para concluir este modelo.
     */
    public abstract int calcularTempoProducao();

    /**
     * Retorna a categoria do modelo exibida no armazém.
     *
     * @return A classificação de qualidade deste modelo (ex.: Processador de Alta Qualidade).
     */
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

    /**
     * Retorna quanto wafer uma unidade deste modelo consome.
     *
     * @return Matéria-prima por unidade, em mm2.
     */
    public int getQuantidadeMateriaPrimaPorUnidade() { return quantidadeMateriaPrimaPorUnidade; }

    /**
     * Retorna o identificador sequencial da unidade.
     *
     * @return Identificador da unidade (ex.: CPU-7).
     */
    public String getId() { return id; }

    /**
     * Retorna o nome do modelo.
     *
     * @return Nome do modelo (ex.: Turing-X4).
     */
    public String getNome() { return nome; }

    /**
     * Retorna em que etapa da linha a unidade está.
     *
     * @return Status atual do produto.
     */
    public StatusProduto getStatus() { return status; }

    /**
     * Retorna o padrão de qualidade do modelo; quanto maior, mais rígida a inspeção.
     *
     * @return Qualidade do modelo, entre 0 e 1.
     */
    public float getQualidade() { return qualidade; }

    /**
     * Atualiza a etapa da unidade na linha (usado pelas estações).
     *
     * @param status Novo status do produto.
     */
    public void setStatus(StatusProduto status) { this.status = status; }

    /**
     * Retorna o risco acumulado pelas falhas das máquinas durante a fabricação.
     *
     * @return Probabilidade de falha acumulada, entre 0 e 1.
     */
    public float getProbabilidadeFalhaAcumulada() { return probabilidadeFalhaAcumulada; }
}