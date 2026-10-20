/**
 * Enumeração dos estados pelos quais um processador passa na linha de montagem.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public enum StatusProduto {
    /** Recém-criado, ainda não passou pela litografia. */
    AGUARDANDO_PROCESSAMENTO,
    /** Já foi processado e aguarda a inspeção da metrologia. */
    PROCESSADO,
    /** Aprovado na metrologia e enviado ao armazém. */
    INSPECIONADO,
    /** Reprovado na metrologia por defeito detectado no die. */
    REJEITADO
}