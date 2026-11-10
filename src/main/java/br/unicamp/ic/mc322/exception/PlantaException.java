package br.unicamp.ic.mc322.exception;

/**
 * Exceção base da hierarquia de erros de domínio da Smart Foundry.
 * Herda de {@link RuntimeException} para representar infrações nas regras de negócio 
 * (como operações inválidas de fábrica) sem obrigar a espalhar blocos try-catch 
 * por todas as camadas do sistema, centralizando a captura na interface principal.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class PlantaException extends RuntimeException {
    
    /**
     * Constrói a exceção genérica da planta com uma mensagem descritiva do erro.
     *
     * @param mensagem A mensagem detalhando o motivo da falha na operação.
     */
    public PlantaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Constrói a exceção guardando o erro original que a causou (por exemplo,
     * uma falha de leitura de arquivo), para não perder o diagnóstico.
     *
     * @param mensagem A mensagem detalhando o motivo da falha na operação.
     * @param causa O erro original.
     */
    public PlantaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}