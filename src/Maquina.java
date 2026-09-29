import java.util.Random;

public abstract class Maquina implements Auditavel {
    private String nome;
    private boolean ligada;
    private int capacidadeMaxima;
    private float probabilidadeFalha;
    private double custoOperacao;
    private Random randomNum;

    // Atributos de saude e manutencao
    private double saude;
    private static final double LIMIAR_CRITICO = 30.0;

    public Maquina(String nome, int capacidadeMaxima, float probabilidadeFalha, double custoOperacao) {
        this.nome = nome;
        this.ligada = false;
        this.capacidadeMaxima = capacidadeMaxima;
        this.probabilidadeFalha = probabilidadeFalha;
        this.custoOperacao = custoOperacao;
        this.randomNum = new Random();
        this.saude = 100.0; // Inicia a 100%
    }

    // Reduz a saude aleatoriamente entre 0 e 3 pontos a cada uso.
    public void aplicarDesgaste() {
        double perda = randomNum.nextDouble() * 3.0;
        this.saude = Math.max(0.0, this.saude - perda);
    }

    // Sobrecarga para permitir que o cenario (Ideal vs Apocaliptico) altere a intensidade do desgaste.
    public void aplicarDesgaste(double multiplicadorCenario) {
        double perda = (randomNum.nextDouble() * 3.0) * multiplicadorCenario;
        this.saude = Math.max(0.0, this.saude - perda);
    }

    // Probabilidade de falha inversamente proporcional a saude.
    protected boolean verificarFalha() {
        if (this.saude <= 0.0) {
            return true; // Maquina avariada nao opera com sucesso
        }

        // Incremento proporcional a saude perdida
        float acrescimoDesgaste = (float) ((100.0 - this.saude) / 100.0) * 0.4f;
        float probabilidadeReal = Math.min(1.0f, this.probabilidadeFalha + acrescimoDesgaste);

        return randomNum.nextFloat() <= probabilidadeReal;
    }

    protected boolean verificarFalha(float probabilidadeCustomizada) {
        if (this.saude <= 0.0) {
            return true;
        }

        float acrescimoDesgaste = (float) ((100.0 - this.saude) / 100.0) * 0.4f;
        float probabilidadeReal = Math.min(1.0f, probabilidadeCustomizada + acrescimoDesgaste);

        return randomNum.nextFloat() <= probabilidadeReal;
    }

    // --- Metodos da Interface Auditavel ---

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
            getTipo(),
            this.nome,
            this.saude,
            this.probabilidadeFalha * 100.0f,
            estado,
            this.ligada
        );
    }

    // --- Metodos de ciclo de vida e estado ---

    public abstract boolean processar(Produto produto);

    public abstract String getTipo();

    public void ligar() {
        ligada = true;
    }

    public void desligar() {
        ligada = false;
    }

    public String getNome() {
        return nome;
    }

    public double getCustoOperacao() {
        return custoOperacao;
    }

    public boolean estaLigada() {
        return ligada;
    }

    public float getProbabilidadeFalha() {
        return probabilidadeFalha;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public double getSaude() {
        return saude;
    }

    public void setSaude(double saude) {
        this.saude = Math.min(100.0, Math.max(0.0, saude));
    }
}