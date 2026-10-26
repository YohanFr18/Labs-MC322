import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa uma linha de produção que agrupa e gere um conjunto de máquinas.
 *
 * <p>Esta classe implementa uma relação de agregação (1:N) com {@link Maquina}.
 * As máquinas são instanciadas externamente e adicionadas à linha, não tendo
 * o seu ciclo de vida estritamente controlado por esta classe.</p>
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class LinhaDeProducao {
    
    private List<Maquina> maquinas;

    /**
     * Constrói uma nova Linha de Produção vazia.
     */
    public LinhaDeProducao() {
        this.maquinas = new ArrayList<>();
    }

    /**
     * Adiciona uma máquina à linha de produção.
     *
     * @param maquina A máquina a ser adicionada. Não faz nada se for nula.
     */
    public void adicionarMaquina(Maquina maquina) {
        if (maquina != null) {
            this.maquinas.add(maquina);
        }
    }

    /**
     * Remove uma máquina específica da linha de produção.
     *
     * @param maquina A máquina a ser removida.
     */
    public void removerMaquina(Maquina maquina) {
        this.maquinas.remove(maquina);
    }

    /**
     * Retorna a lista de máquinas presentes na linha de produção.
     * <p>A lista retornada é imutável para proteger o encapsulamento interno da classe.</p>
     *
     * @return Uma lista não modificável de máquinas.
     */
    public List<Maquina> getMaquinas() {
        return Collections.unmodifiableList(this.maquinas);
    }
}