import java.util.Random;

/**
 * Classe abstrata que representa uma máquina genérica na linha de produção.
 * Fornece a infraestrutura comum para o estado, saúde operacional, desgaste e auditoria de qualquer máquina.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public abstract class Maquina implements Auditavel {
    private String nome;
    private boolean ligada;
    private int capacidadeMaxima;
    private float probabilidadeFalha;
    private double custoOperacao;
    private Random randomNum;
    private double saude;
    private static final double LIMIAR_CRITICO = 30.0;
    private float multiplicadorFalhaCenario;

    /**
     * Construtor da Máquina.
     * Inicializa a saúde a 100% e o multiplicador de falha consoante o cenário padrão.
     *
     * @param nome Nome identificador da máquina.
     * @param capacidadeMaxima Limite máximo de processamento (se aplicável).
     * @param probabilidadeFalha Risco base inerente de a máquina cometer um erro durante a operação.
     * @param custoOperacao Custo financeiro debitado do orçamento a cada ciclo de operação.
     */
    public Maquina(String nome, int capacidadeMaxima, float probabilidadeFalha, double custoOperacao) {
        this.nome = nome;
        this.ligada = false;
        this.capacidadeMaxima = capacidadeMaxima;
        this.probabilidadeFalha = probabilidadeFalha;
        this.custoOperacao = custoOperacao;
        this.randomNum = new Random();
        this.saude = 100.0;
        this.multiplicadorFalhaCenario = 1.0f;
    }

    public void setMultiplicadorFalhaCenario(float multiplicadorFalhaCenario) {
        this.multiplicadorFalhaCenario = multiplicadorFalhaCenario;
    }

    /**
     * Verifica se a máquina sofreu avaria total.
     * @return {@code true} se a saúde for 0.0 ou inferior, indicando que a máquina partiu.
     */
    public boolean estaQuebrada() {
        return this.saude <= 0.0;
    }

    /**
     * Aplica um desgaste aleatório à saúde da máquina inerente ao seu uso contínuo.
     * O desgaste varia entre 0 e 3 pontos percentuais a cada ciclo.
     */
    public void aplicarDesgaste() {
        double perda = randomNum.nextDouble() * 3.0;
        this.saude = Math.max(0.0, this.saude - perda);
    }

    /**
     * Aplica um desgaste influenciado pela severidade do cenário ativo (Ideal vs. Apocalíptico).
     *
     * @param multiplicadorCenario Fator multiplicativo sobre o desgaste base gerado.
     */
    public void aplicarDesgaste(double multiplicadorCenario) {
        double perda = (randomNum.nextDouble() * 3.0) * multiplicadorCenario;
        this.saude = Math.max(0.0, this.saude - perda);
    }

    /**
     * Calcula se ocorreu uma falha na operação com base no desgaste atual da máquina e no cenário.
     * A probabilidade de falha é inversamente proporcional à saúde restante.
     *
     * @return {@code true} se a operação falhou, {@code false} se for bem-sucedida.
     */
    protected boolean verificarFalha() {
        if (this.saude <= 0.0) {
            return true;
        }
        float acrescimoDesgaste = (float) ((100.0 - this.saude) / 100.0) * 0.4f;
        float probabilidadeReal = Math.min(1.0f, (this.probabilidadeFalha + acrescimoDesgaste) * multiplicadorFalhaCenario);
        return randomNum.nextFloat() <= probabilidadeReal;
    }

    /**
     * Calcula a ocorrência de falha permitindo injetar uma probabilidade customizada.
     *
     * @param probabilidadeCustomizada Valor de falha específico injetado pelas subclasses.
     * @return {@code true} se a operação falhou, {@code false} se for bem-sucedida.
     */
    protected boolean verificarFalha(float probabilidadeCustomizada) {
        if (this.saude <= 0.0) {
            return true;
        }
        float acrescimoDesgaste = (float) ((100.0 - this.saude) / 100.0) * 0.4f;
        float probabilidadeReal = Math.min(1.0f, (probabilidadeCustomizada + acrescimoDesgaste) * multiplicadorFalhaCenario);
        return randomNum.nextFloat() <= probabilidadeReal;
    }

    @Override
    public boolean precisaManutencao() {
        return this.saude < LIMIAR_CRITICO;
    }

    @Override
    public String gerarRelatorioDiagnostico() {
        String estado;
        if (this.saude <= 0.0) {
            estado = "AVARIADA/QUEBRADA";
        } else if (precisaManutencao()) {
            estado = "REQUER MANUTENCAO";
        } else {
            estado = "OPERACIONAL";
        }

        return String.format(
            "[%s] %s | Saude: %.1f%% | Falha Base: %.1f%% | Estado: %s | Ligada: %b",
            getTipo(), this.nome, this.saude, this.probabilidadeFalha * 100.0f, estado, this.ligada
        );
    }

    /**
     * Processa um único wafer/produto nesta fase específica da linha de montagem.
     *
     * @param produto Instância do produto que está a passar pela máquina.
     * @return {@code true} se o processamento avançou com sucesso, {@code false} em caso de bloqueio ou erro grave.
     */
    public abstract boolean processar(Produto produto);

    /**
     * Retorna o tipo funcional da máquina para efeitos de registo e auditoria.
     *
     * @return String identificando o tipo (ex: "Processamento", "Inspeção").
     */
    public abstract String getTipo();

    public void ligar() { ligada = true; }
    public void desligar() { ligada = false; }
    public String getNome() { return nome; }
    public double getCustoOperacao() { return custoOperacao; }
    public boolean estaLigada() { return ligada; }
    public float getProbabilidadeFalha() { return probabilidadeFalha; }
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    public double getSaude() { return saude; }
    public void setSaude(double saude) { this.saude = Math.min(100.0, Math.max(0.0, saude)); }
}