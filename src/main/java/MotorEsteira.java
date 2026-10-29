/**
 * Representa o motor responsável por movimentar os wafers dentro da máquina.
 * É uma parte essencial (1:1) do ciclo de vida da máquina.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class MotorEsteira {
    
    private double desgasteAcumulado;
    private static final double LIMITE_FALHA = 100.0;

    /**
     * Construtor padrão. Inicializa o motor sem desgaste.
     */
    public MotorEsteira() {
        this.desgasteAcumulado = 0.0;
    }

    /**
     * Verifica se o motor atingiu o limite de desgaste e entrou em falha.
     *
     * @return {@code true} se o desgaste acumulado for maior ou igual ao limite.
     */
    public boolean isEmFalha() {
        return this.desgasteAcumulado >= LIMITE_FALHA;
    }

    /**
     * Aumenta o desgaste do motor com base no cenário operacional.
     *
     * @param multiplicadorCenario Fator multiplicativo de desgaste.
     */
    public void aumentarDesgaste(double multiplicadorCenario) {
        // O motor desgasta-se um pouco a cada ciclo
        this.desgasteAcumulado += (2.5 * multiplicadorCenario);
    }

    /**
     * Repara o motor, devolvendo o desgaste ao estado inicial (0.0).
     */
    public void reparar() {
        this.desgasteAcumulado = 0.0;
    }

    /**
     * Devolve o estado atual do motor para ser utilizado no relatório de auditoria em cascata.
     *
     * @return Uma string formatada com a percentagem de desgaste e o estado.
     */
    public String obterStatus() {
        String estado = isEmFalha() ? "FALHA CRITICA" : "A Funcionar";
        return String.format("%s (Desgaste: %.1f%%)", estado, Math.min(this.desgasteAcumulado, 100.0));
    }
}