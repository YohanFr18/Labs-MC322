/**
 * Exceção lançada quando uma etapa da produção tenta utilizar uma máquina que
 * não está apta para operar (por exemplo, por estar avariada/quebrada 
 * com saúde zero, ou desligada).
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class MaquinaIndisponivelException extends PlantaException {
    
    /**
     * Constrói a exceção de indisponibilidade de equipamento.
     *
     * @param mensagem A mensagem indicando qual máquina falhou e o seu estado atual.
     */
    public MaquinaIndisponivelException(String mensagem) {
        super(mensagem);
    }
}