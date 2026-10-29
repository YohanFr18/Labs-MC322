import java.util.Random;

/**
 * Representa um sensor térmico interno da máquina.
 * É utilizado numa relação 1:N com a máquina principal, existindo várias instâncias
 * espalhadas pela linha para garantir o controlo de temperatura.
 * 
 * @author Victor Rimes e Yohan Rocha
 */
public class SensorTermico {
    
    private String id;
    private double calibracao;
    private double temperaturaAtual;
    private Random geradorTemperaturas;

    /**
     * Construtor do sensor.
     * 
     * @param id Identificador único do sensor (ex: "Sensor-1").
     */
    public SensorTermico(String id) {
        this.id = id;
        this.calibracao = 100.0;
        this.geradorTemperaturas = new Random();
        this.temperaturaAtual = 22.0; // Temperatura ambiente padrão
    }

    /**
     * Verifica se o sensor perdeu a calibração na totalidade, falhando.
     * 
     * @return {@code true} se a calibração for igual ou inferior a 0.
     */
    public boolean isFalhou() {
        return this.calibracao <= 0.0;
    }

    /**
     * Simula a leitura da temperatura atual e verifica se está acima do limite seguro (90ºC).
     * Sensores descalibrados têm maior probabilidade de gerar leituras falsas altas.
     * 
     * @return {@code true} se relatar sobreaquecimento.
     */
    public boolean isSobreaquecido() {
        if (isFalhou()) return true;
        
        // Simulação básica: temperatura flutua mais se a calibração estiver baixa
        double erroLeitura = (100.0 - this.calibracao) * 0.5;
        this.temperaturaAtual = 70.0 + geradorTemperaturas.nextDouble() * 10.0 + erroLeitura;
        
        return this.temperaturaAtual > 90.0;
    }

    /**
     * Desgasta a calibração do sensor ao longo do tempo.
     * 
     * @param multiplicadorCenario Fator multiplicativo de desgaste.
     */
    public void aumentarDesgaste(double multiplicadorCenario) {
        this.calibracao -= (1.0 * multiplicadorCenario);
        if (this.calibracao < 0.0) {
            this.calibracao = 0.0;
        }
    }

    /**
     * Recalibra o sensor para 100%, restaurando o seu funcionamento perfeito.
     */
    public void reparar() {
        this.calibracao = 100.0;
        this.temperaturaAtual = 22.0;
    }

    /**
     * Devolve o estado do sensor para ser incluído no relatório de auditoria do Todo.
     * 
     * @return String formatada com o ID do sensor, a temperatura e a sua calibração.
     */
    public String obterStatus() {
        if (isFalhou()) {
            return String.format("[%s] INOPERANTE (Descalibrado)", this.id);
        }
        return String.format("[%s] Calibração: %.1f%% | Leitura: %.1fºC", 
                this.id, this.calibracao, this.temperaturaAtual);
    }
}