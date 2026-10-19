/**
 * Exceção lançada quando ocorre uma tentativa de manipular uma demanda de forma inconsistente.
 * Pode ocorrer ao tentar fabricar uma demanda inexistente, criar demandas com quantidades
 * negativas ou interagir com demandas que já foram canceladas ou concluídas.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class DemandaInvalidaException extends PlantaException {
    
    /**
     * Constrói a exceção de manipulação inválida de demanda.
     *
     * @param mensagem A mensagem explicando por que motivo a operação sobre a demanda não é permitida.
     */
    public DemandaInvalidaException(String mensagem) {
        super(mensagem);
    }
}