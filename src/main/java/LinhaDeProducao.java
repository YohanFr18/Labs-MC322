import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa uma linha de produção que agrupa um conjunto de máquinas.
 *
 * <p>É uma agregação (1:N) com {@link Maquina}, e não uma composição: as
 * máquinas são criadas fora da linha e adicionadas a ela, e uma máquina pode
 * ser retirada de uma linha e instalada em outra sem deixar de existir.</p>
 *
 * <p>Também é {@link Auditavel}: seu relatório pede o relatório de cada
 * máquina, que por sua vez inclui o das suas partes (auditoria em cascata
 * Linha &gt; Máquina &gt; Partes).</p>
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class LinhaDeProducao implements Auditavel {

    private String nome;
    private List<Maquina> maquinas;

    /**
     * Constrói uma nova linha de produção vazia.
     *
     * @param nome Nome da linha, usado nos relatórios (ex.: "Linha Principal").
     */
    public LinhaDeProducao(String nome) {
        this.nome = nome;
        this.maquinas = new ArrayList<>();
    }

    /**
     * Retorna o nome da linha de produção.
     *
     * @return Nome da linha.
     */
    public String getNome() {
        return nome;
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
     * Remove uma máquina específica da linha de produção. A máquina continua
     * existindo e pode ser adicionada a outra linha.
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

    /**
     * A linha precisa de atenção se qualquer uma das suas máquinas precisar de manutenção.
     *
     * @return {@code true} se alguma máquina precisar de manutenção.
     */
    @Override
    public boolean precisaManutencao() {
        for (Maquina m : maquinas) {
            if (m.precisaManutencao()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gera o relatório da linha e, em cascata, o de cada máquina (que inclui
     * suas partes), indentando cada nível abaixo do anterior.
     *
     * @return Relatório hierárquico: linha, máquinas e partes.
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        int emAlerta = 0;
        for (Maquina m : maquinas) {
            if (m.precisaManutencao()) {
                emAlerta++;
            }
        }
        String estado = emAlerta == 0 ? "OPERACIONAL" : "REQUER ATENÇÃO (" + emAlerta + " estação(ões) em alerta)";
        String relatorio = nome + " | " + maquinas.size() + " estação(ões) | Estado: " + estado;

        for (Maquina m : maquinas) {
            // Cada linha do relatório da máquina ganha um nível a mais de indentação
            String relatorioMaquina = m.gerarRelatorioDiagnostico().replace("\n", "\n    ");
            relatorio += "\n    > " + relatorioMaquina;
        }
        return relatorio;
    }
}
