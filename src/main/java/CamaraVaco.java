/**
 * Representa a câmara de vácuo onde ocorre o processamento dos semicondutores.
 * O desgaste afeta as vedações, o que pode causar despressurização e interromper a produção.
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
     * Verifica se a câmara perdeu a pressão necessária para o processamento.
     * 
     * @return {@code true} se a câmara estiver despressurizada.
     */
    public boolean isDespressurizada() {
        return !this.pressurizada;
    }

    /**
     * Desgasta as vedações da câmara. Se a integridade descer abaixo de 20%,
     * a câmara perde a pressão automaticamente.
     * 
     * @param multiplicadorCenario Fator multiplicativo de desgaste.
     */
    public void desgastarVedações(double multiplicadorCenario) {
        this.integridadeVedacoes -= (1.8 * multiplicadorCenario);
        if (this.integridadeVedacoes < 0.0) {
            this.integridadeVedacoes = 0.0;
        }
        
        if (this.integridadeVedacoes < 20.0) {
            this.pressurizada = false;
        }
    }

    /**
     * Tenta repressurizar a câmara. Só é bem-sucedido se as vedações estiverem
     * em bom estado (acima de 20%).
     */
    public void repressurizar() {
        if (this.integridadeVedacoes >= 20.0) {
            this.pressurizada = true;
        }
    }

    /**
     * Repara as vedações da câmara na totalidade e repõe a pressão normal.
     */
    public void reparar() {
        this.integridadeVedacoes = 100.0;
        this.pressurizada = true;
    }

    /**
     * Devolve o estado atual da câmara para o relatório de diagnóstico.
     * 
     * @return String contendo a integridade das vedações e o estado da pressão.
     */
    public String obterStatus() {
        String estadoPressao = this.pressurizada ? "Vácuo Estável" : "VAZAMENTO/DESPRESSURIZADA";
        return String.format("%s | Integridade Vedações: %.1f%%", estadoPressao, this.integridadeVedacoes);
    }
}