package br.unicamp.ic.mc322.exception;

/**
 * Exceção lançada quando não é possível salvar ou carregar o estado da planta:
 * arquivo inexistente ou vazio, XML malformado, falha de leitura/escrita ou
 * dados que violam as regras do domínio.
 *
 * <p>Faz parte da hierarquia de {@link PlantaException}, então a interface
 * pode tratá-la junto com os demais erros da planta. Quando o problema vem de
 * outra exceção (por exemplo, uma {@code IOException}), ela é guardada como
 * causa.</p>
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class PersistenciaException extends PlantaException {

    /**
     * Constrói a exceção com uma mensagem descritiva.
     *
     * @param mensagem O motivo pelo qual salvar ou carregar falhou.
     */
    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Constrói a exceção guardando o erro original.
     *
     * @param mensagem O motivo pelo qual salvar ou carregar falhou.
     * @param causa O erro original (de E/S, de formato ou de regra de domínio).
     */
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
