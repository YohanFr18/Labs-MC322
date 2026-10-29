import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classe abstrata que representa uma máquina genérica na linha de produção.
 * Fornece a infraestrutura comum para o estado, saúde operacional, desgaste,
 * auditoria e composição estrita das suas partes internas.
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

    // --- Composição Estrita (1:1 e 1:N) ---
    private MotorEsteira motor;
    private CamaraVaco camara;
    private List<SensorTermico> sensores;

    /**
     * Construtor da Máquina.
     * Inicializa a saúde a 100%, o multiplicador de falha e instancia estritamente
     * as partes internas (Item 5: Fases de Construção).
     *
     * @param nome Nome identificador da máquina.
     * @param capacidadeMaxima Limite máximo de processamento (se aplicável).
     * @param probabilidadeFalha Risco base inerente de a máquina cometer um erro durante a operação.
     * @param custoOperacao Custo financeiro debitado do orçamento a cada ciclo de operação.
     * @param qtdSensores Número de sensores térmicos a serem instalados (configuração base para as partes).
     */
    public Maquina(String nome, int capacidadeMaxima, float probabilidadeFalha, double custoOperacao, int qtdSensores) {
        this.nome = nome;
        this.ligada = false;
        this.capacidadeMaxima = capacidadeMaxima;
        this.probabilidadeFalha = probabilidadeFalha;
        this.custoOperacao = custoOperacao;
        this.randomNum = new Random();
        this.saude = 100.0;
        this.multiplicadorFalhaCenario = 1.0f;

        // Item 5: A máquina instancia as suas próprias partes. O ciclo de vida pertence à máquina.
        this.motor = new MotorEsteira();
        this.camara = new CamaraVaco();
        this.sensores = new ArrayList<>();
        
        for (int i = 0; i < qtdSensores; i++) {
            this.sensores.add(new SensorTermico("Sensor-" + (i + 1)));
        }
    }

    /**
     * Define o multiplicador do cenário ativo, aplicado sobre a probabilidade de falha.
     *
     * @param multiplicadorFalhaCenario Fator do cenário (acima de 1 aumenta as falhas).
     */
    public void setMultiplicadorFalhaCenario(float multiplicadorFalhaCenario) {
        this.multiplicadorFalhaCenario = multiplicadorFalhaCenario;
    }

    /**
     * Item 6: Uma parte quebrada afeta o Todo.
     * Verifica se a máquina sofreu avaria total (saúde zerada) ou se alguma das 
     * partes essenciais falhou gravemente.
     * 
     * @return {@code true} se a máquina estiver quebrada.
     */
    public boolean estaQuebrada() {
        if (this.saude <= 0.0) {
            return true;
        }

        // Propagação do estado: se o motor ou a câmara falham, a máquina inteira pára.
        if (motor.isEmFalha() || camara.isDespressurizada()) {
            return true;
        }

        // Se mais de 50% dos sensores estiverem em falha, a máquina é considerada inoperante.
        long sensoresInoperantes = sensores.stream().filter(SensorTermico::isFalhou).count();
        if (sensoresInoperantes > (sensores.size() / 2)) {
            return true;
        }

        return false;
    }

    /**
     * Aplica um desgaste influenciado pela severidade do cenário ativo.
     * O desgaste espalha-se também pelas partes internas.
     *
     * @param multiplicadorCenario Fator multiplicativo sobre o desgaste base gerado.
     */
    public void aplicarDesgaste(double multiplicadorCenario) {
        double perda = (randomNum.nextDouble() * 3.0) * multiplicadorCenario;
        this.saude = Math.max(0.0, this.saude - perda);

        // Propaga o desgaste mecânico e térmico para as partes
        motor.aumentarDesgaste(multiplicadorCenario);
        camara.desgastarVedações(multiplicadorCenario);
        for (SensorTermico s : sensores) {
            s.aumentarDesgaste(multiplicadorCenario);
        }
    }

    /**
     * Item 7: Lei de Deméter (Desacoplamento).
     * O sistema exterior não sabe que os sensores existem, mas pode perguntar à máquina
     * se a temperatura global está estável.
     * 
     * @return {@code true} se nenhum sensor relatar sobreaquecimento.
     */
    public boolean isTemperaturaEstavel() {
        for (SensorTermico s : sensores) {
            if (s.isSobreaquecido()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Item 7: Lei de Deméter (Desacoplamento).
     * Delega a reinicialização da pressão para a câmara de vácuo interna.
     */
    public void recalibrarPressaoCamara() {
        camara.repressurizar();
    }

    @Override
    public boolean precisaManutencao() {
        return this.saude < LIMIAR_CRITICO || estaQuebrada();
    }

    /**
     * Define a saúde da máquina. Quando reparada (100%), repara em cascata
     * as partes que compõem a máquina.
     *
     * @param saude Nova saúde da máquina.
     */
    public void setSaude(double saude) { 
        this.saude = Math.min(100.0, Math.max(0.0, saude)); 
        
        // Reparação em cascata das partes (Ownership do ciclo de vida)
        if (this.saude == 100.0) {
            motor.reparar();
            camara.reparar();
            for (SensorTermico s : sensores) {
                s.reparar();
            }
        }
    }

    /**
     * Calcula se ocorreu uma falha na operação.
     */
    protected boolean verificarFalha() {
        if (estaQuebrada()) {
            return true;
        }
        float acrescimoDesgaste = (float) ((100.0 - this.saude) / 100.0) * 0.4f;
        float probabilidadeReal = Math.min(1.0f, (this.probabilidadeFalha + acrescimoDesgaste) * multiplicadorFalhaCenario);
        return randomNum.nextFloat() <= probabilidadeReal;
    }

    @Override
    public String gerarRelatorioDiagnostico() {
        String estado;
        if (estaQuebrada()) {
            estado = "AVARIADA/QUEBRADA";
        } else if (precisaManutencao()) {
            estado = "REQUER MANUTENCAO";
        } else {
            estado = "OPERACIONAL";
        }

        // Auditoria em cascata: O Todo reporta o seu estado e pede o estado às suas Partes (Ponto 10)
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] %s | Saúde: %.1f%% | Estado: %s\n", 
            getTipo(), this.nome, this.saude, estado));
        
        sb.append("    -> Motor: ").append(motor.obterStatus()).append("\n");
        sb.append("    -> Câmara: ").append(camara.obterStatus()).append("\n");
        
        for (SensorTermico s : sensores) {
            sb.append("    -> ").append(s.obterStatus()).append("\n");
        }
        
        return sb.toString();
    }

    public abstract boolean processar(Produto produto);
    public abstract String getTipo();

    public void ligar() { ligada = true; }
    public void desligar() { ligada = false; }
    
    public String getNome() { return nome; }
    public double getCustoOperacao() { return custoOperacao; }
    public boolean estaLigada() { return ligada; }
    public float getProbabilidadeFalha() { return probabilidadeFalha; }
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    public double getSaude() { return saude; }
}