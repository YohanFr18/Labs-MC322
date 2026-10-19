/**
 * Interface que define o contrato para componentes da fábrica que podem ser auditados.
 * Permite a verificação de saúde e geração de relatórios tanto de máquinas como de produtos.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public interface Auditavel {
    
    /**
     * Verifica se o componente necessita de manutenção ou intervenção.
     *
     * @return {@code true} se o componente estiver em estado crítico, {@code false} caso contrário.
     */
    boolean precisaManutencao();

    /**
     * Gera um relatório textual com o diagnóstico do estado atual do componente.
     *
     * @return String contendo o formato formatado do diagnóstico.
     */
    String gerarRelatorioDiagnostico();
}