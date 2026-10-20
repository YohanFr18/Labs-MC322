/**
 * Exceção lançada quando uma operação fabril é abortada devido à falta de recursos essenciais,
 * tais como orçamento (budget) insuficiente ou falta de matéria-prima (wafers de silício) no armazém.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class RecursoInsuficienteException extends PlantaException {
    
    /**
     * Constrói a exceção de recursos insuficientes.
     *
     * @param mensagem A mensagem detalhando qual recurso faltou e a quantidade necessária.
     */
    public RecursoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}