package br.unicamp.ic.mc322.model;

import java.util.Random;

/**
 * Representa um sensor térmico interno da máquina.
 * Cada {@link Maquina} tem vários sensores (relação 1:N), criados no
 * construtor dela, para monitorar a temperatura do processo.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class SensorTermico {

    private static final double TEMPERATURA_LIMITE = 90.0;

    private String id;
    private double calibracao;
    private double temperaturaAtual;
    private Random geradorTemperaturas;

    /**
     * Construtor do sensor.
     *
     * @param id Identificador do sensor dentro da máquina (ex.: "Sensor-1").
     */
    public SensorTermico(String id) {
        this.id = id;
        this.calibracao = 100.0;
        this.geradorTemperaturas = new Random();
        this.temperaturaAtual = 22.0; // Temperatura ambiente padrão
    }

    /**
     * Verifica se o sensor perdeu toda a calibração e deixou de funcionar.
     *
     * @return {@code true} se a calibração for menor ou igual a 0.
     */
    public boolean isFalhou() {
        return this.calibracao <= 0.0;
    }

    /**
     * Faz uma nova leitura de temperatura e verifica se ela passou do limite
     * seguro (90 ºC). Quanto pior a calibração, maior o erro da leitura.
     * Um sensor que falhou sempre indica superaquecimento.
     *
     * @return {@code true} se a leitura indicar superaquecimento.
     */
    public boolean isSuperaquecido() {
        if (isFalhou()) {
            return true;
        }

        // A temperatura oscila mais quando a calibração está baixa
        double erroLeitura = (100.0 - this.calibracao) * 0.5;
        this.temperaturaAtual = 70.0 + geradorTemperaturas.nextDouble() * 10.0 + erroLeitura;

        return this.temperaturaAtual > TEMPERATURA_LIMITE;
    }

    /**
     * Reduz a calibração do sensor a cada ciclo de uso.
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
     * Recalibra o sensor para 100%, restaurando o funcionamento normal.
     */
    public void reparar() {
        this.calibracao = 100.0;
        this.temperaturaAtual = 22.0;
    }

    // Leitura e restauração usadas só pela Maquina (mesmo pacote) ao salvar/carregar a planta.
    // A temperatura não é restaurada: é uma leitura momentânea, refeita a cada ciclo.
    double getCalibracao() {
        return this.calibracao;
    }

    void restaurarCalibracao(double calibracao) {
        if (calibracao < 0.0 || calibracao > 100.0 || Double.isNaN(calibracao)) {
            throw new IllegalArgumentException("Calibração do " + id + " fora de 0-100: " + calibracao);
        }
        this.calibracao = calibracao;
    }

    /**
     * Retorna o estado do sensor para a auditoria em cascata.
     *
     * @return Texto com o identificador, a calibração e a última leitura.
     */
    public String obterStatus() {
        if (isFalhou()) {
            return String.format("%s: INOPERANTE (descalibrado)", this.id);
        }
        String alerta = this.temperaturaAtual > TEMPERATURA_LIMITE ? " SUPERAQUECIDO" : "";
        return String.format("%s: Calibração: %.1f%% | Leitura: %.1f ºC%s",
                this.id, this.calibracao, this.temperaturaAtual, alerta);
    }
}
