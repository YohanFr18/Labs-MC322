package br.unicamp.ic.mc322.model;

/**
 * Representa a câmara de vácuo onde o wafer é processado.
 * É uma parte essencial (1:1) da {@link Maquina}. O desgaste afeta as
 * vedações e, abaixo de 20% de integridade, a câmara perde o vácuo e a
 * estação para.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class CamaraVaco {

    private double integridadeVedacoes;
    private boolean pressurizada;

    /**
     * Construtor padrão. Inicializa a câmara com as vedações a 100% e pressurizada.
     */
    public CamaraVaco() {
        this.integridadeVedacoes = 100.0;
        this.pressurizada = true;
    }

    /**
     * Verifica se a câmara perdeu o vácuo necessário para o processamento.
     *
     * @return {@code true} se a câmara estiver despressurizada.
     */
    public boolean isDespressurizada() {
        return !this.pressurizada;
    }

    /**
     * Desgasta as vedações da câmara. Se a integridade ficar abaixo de 20%,
     * a câmara perde a pressão automaticamente.
     *
     * @param multiplicadorCenario Fator multiplicativo de desgaste.
     */
    public void desgastarVedacoes(double multiplicadorCenario) {
        this.integridadeVedacoes -= (1.8 * multiplicadorCenario);
        if (this.integridadeVedacoes < 0.0) {
            this.integridadeVedacoes = 0.0;
        }

        if (this.integridadeVedacoes < 20.0) {
            this.pressurizada = false;
        }
    }

    /**
     * Tenta repressurizar a câmara. Só funciona se as vedações estiverem
     * em bom estado (integridade de pelo menos 20%).
     */
    public void repressurizar() {
        if (this.integridadeVedacoes >= 20.0) {
            this.pressurizada = true;
        }
    }

    /**
     * Troca as vedações da câmara e restaura o vácuo.
     */
    public void reparar() {
        this.integridadeVedacoes = 100.0;
        this.pressurizada = true;
    }

    // Leitura e restauração usadas só pela Maquina (mesmo pacote) ao salvar/carregar a planta
    double getIntegridadeVedacoes() {
        return this.integridadeVedacoes;
    }

    boolean isPressurizada() {
        return this.pressurizada;
    }

    void restaurar(double integridadeVedacoes, boolean pressurizada) {
        if (integridadeVedacoes < 0.0 || integridadeVedacoes > 100.0 || Double.isNaN(integridadeVedacoes)) {
            throw new IllegalArgumentException("Integridade das vedações fora de 0-100: " + integridadeVedacoes);
        }
        // Mesma regra de desgastarVedacoes/repressurizar: abaixo de 20% não existe vácuo
        if (pressurizada && integridadeVedacoes < 20.0) {
            throw new IllegalArgumentException("Câmara não pode estar pressurizada com vedações a "
                    + integridadeVedacoes + "% (mínimo de 20%).");
        }
        this.integridadeVedacoes = integridadeVedacoes;
        this.pressurizada = pressurizada;
    }

    /**
     * Retorna o estado atual da câmara para a auditoria em cascata.
     *
     * @return Texto com o estado do vácuo e a integridade das vedações.
     */
    public String obterStatus() {
        String estadoPressao = this.pressurizada ? "Vácuo estável" : "VAZAMENTO/DESPRESSURIZADA";
        return String.format("Câmara de vácuo: %s (Vedações: %.1f%%)", estadoPressao, this.integridadeVedacoes);
    }
}
