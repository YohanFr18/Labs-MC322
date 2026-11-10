package br.unicamp.ic.mc322.model;

/**
 * Representa o motor que movimenta os wafers dentro de uma estação.
 * É uma parte essencial (1:1) da {@link Maquina}: criado no construtor dela
 * e sem existência fora dela.
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
     * Aumenta o desgaste do motor a cada ciclo, conforme o cenário de operação.
     *
     * @param multiplicadorCenario Fator multiplicativo de desgaste.
     */
    public void aumentarDesgaste(double multiplicadorCenario) {
        this.desgasteAcumulado += (2.5 * multiplicadorCenario);
    }

    /**
     * Repara o motor, zerando o desgaste acumulado.
     */
    public void reparar() {
        this.desgasteAcumulado = 0.0;
    }

    // Leitura e restauração usadas só pela Maquina (mesmo pacote) ao salvar/carregar a planta
    double getDesgasteAcumulado() {
        return this.desgasteAcumulado;
    }

    void restaurarDesgaste(double desgasteAcumulado) {
        if (desgasteAcumulado < 0.0 || Double.isNaN(desgasteAcumulado)) {
            throw new IllegalArgumentException("Desgaste do motor inválido: " + desgasteAcumulado);
        }
        this.desgasteAcumulado = desgasteAcumulado;
    }

    /**
     * Retorna o estado atual do motor para a auditoria em cascata.
     *
     * @return Texto com o estado e a porcentagem de desgaste.
     */
    public String obterStatus() {
        String estado = isEmFalha() ? "FALHA CRÍTICA" : "Funcionando";
        return String.format("Motor da esteira: %s (Desgaste: %.1f%%)", estado, Math.min(this.desgasteAcumulado, 100.0));
    }
}
