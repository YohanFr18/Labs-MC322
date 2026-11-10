package br.unicamp.ic.mc322.model;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;

/**
 * Encomenda de um modelo de processador, com a quantidade de unidades que
 * ainda falta fabricar e o seu {@link StatusDemanda}.
 *
 * <p>Ciclo de vida: uma demanda {@code PENDENTE} passa a {@code EM_PRODUCAO}
 * enquanto uma unidade está na linha e volta a {@code PENDENTE} quando a
 * unidade termina (ou é interrompida). Ao fabricar a última unidade ela fica
 * {@code CONCLUIDA}. Se faltar wafer ou budget, o gerenciador a marca como
 * {@code CANCELADA}; atualizar a quantidade para um valor positivo a reativa.</p>
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class Demanda {
    private String tipoProduto;
    private int quantidadeProdutos;
    private StatusDemanda status;

    /**
     * Cria uma demanda pendente.
     *
     * @param tipoProduto Nome do modelo encomendado (ex.: "Turing-X4").
     * @param quantidadeProdutos Unidades encomendadas; valores negativos viram zero.
     */
    public Demanda(String tipoProduto, int quantidadeProdutos) {
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = Math.max(0, quantidadeProdutos);
        this.status = StatusDemanda.PENDENTE;
    }

    /**
     * Recria uma demanda salva anteriormente, com a quantidade e o status que
     * ela tinha. Recusa combinações que o ciclo de vida normal nunca produz.
     *
     * <p>{@code EM_PRODUCAO} é recusado porque é um estado transitório: só existe
     * enquanto uma unidade atravessa a linha, nunca entre duas operações.</p>
     *
     * @param tipoProduto Nome do modelo encomendado.
     * @param quantidadeProdutos Unidades que ainda faltavam fabricar.
     * @param status Status que a demanda tinha.
     * @return A demanda restaurada.
     * @throws DemandaInvalidaException Se a quantidade for negativa, se o status
     *                                  for {@code EM_PRODUCAO} ou se uma demanda
     *                                  {@code CONCLUIDA} ainda tiver unidades.
     */
    public static Demanda restaurar(String tipoProduto, int quantidadeProdutos, StatusDemanda status) {
        if (quantidadeProdutos < 0) {
            throw new DemandaInvalidaException("Demanda de " + tipoProduto + " com quantidade negativa ("
                    + quantidadeProdutos + ").");
        }
        if (status == StatusDemanda.EM_PRODUCAO) {
            throw new DemandaInvalidaException("Demanda de " + tipoProduto
                    + " não pode ser restaurada no meio da produção (EM_PRODUCAO).");
        }
        if (status == StatusDemanda.CONCLUIDA && quantidadeProdutos > 0) {
            throw new DemandaInvalidaException("Demanda de " + tipoProduto + " está concluída mas ainda tem "
                    + quantidadeProdutos + " unidade(s) a fabricar.");
        }
        Demanda demanda = new Demanda(tipoProduto, quantidadeProdutos);
        demanda.status = status;
        return demanda;
    }

    /**
     * Redefine quantas unidades ainda devem ser fabricadas. Uma quantidade
     * positiva reativa a demanda (volta a {@code PENDENTE}), inclusive se ela
     * tiver sido cancelada ou concluída.
     *
     * @param novaQuantidade Nova quantidade de unidades.
     * @throws DemandaInvalidaException Se {@code novaQuantidade} for negativa.
     */
    public void atualizarQuantidade(int novaQuantidade) {
        if (novaQuantidade < 0) {
            throw new DemandaInvalidaException("A quantidade de uma demanda não pode ser negativa ("
                    + novaQuantidade + ").");
        }

        this.quantidadeProdutos = novaQuantidade;

        // Uma quantidade positiva reativa a demanda, inclusive se ela tiver sido cancelada
        if (novaQuantidade > 0) {
            this.status = StatusDemanda.PENDENTE;
        }
    }

    /**
     * Calcula quanta matéria-prima a demanda inteira consumiria.
     *
     * @param quantidadePorUnidade Matéria-prima gasta por unidade.
     * @return O total necessário, ou 0 se algum dos valores não for positivo.
     */
    public int calcularMateriaPrimaNecessaria(int quantidadePorUnidade) {
        if (quantidadePorUnidade <= 0 || quantidadeProdutos <= 0) {
            return 0;
        }
        return this.quantidadeProdutos * quantidadePorUnidade;
    }

    /**
     * Verifica se o orçamento cobre a matéria-prima de todas as unidades restantes.
     *
     * @param orcamentoDisponivel Budget disponível, em reais.
     * @param custoPorUnidadeMateriaPrima Preço de cada unidade de matéria-prima.
     * @param quantidadeMateriaPrimaPorUnidade Matéria-prima gasta por processador.
     * @return {@code true} se o custo total couber no orçamento.
     */
    public boolean verificarViabilidadeFinanceira(double orcamentoDisponivel, int custoPorUnidadeMateriaPrima,
            int quantidadeMateriaPrimaPorUnidade) {
        int aux = calcularMateriaPrimaNecessaria(quantidadeMateriaPrimaPorUnidade);
        int custoTotal = aux * custoPorUnidadeMateriaPrima;

        return custoTotal <= orcamentoDisponivel;
    }

    /**
     * Informa, sem alterar nada, se a demanda pode entrar na linha agora.
     *
     * @return {@code true} se ela está {@code PENDENTE} e ainda tem unidades a fabricar.
     */
    public boolean podeSerProduzida() {
        return this.status == StatusDemanda.PENDENTE && this.quantidadeProdutos > 0;
    }

    /**
     * Coloca a demanda em produção ({@code PENDENTE -> EM_PRODUCAO}).
     * Quem chama deve conferir antes com {@link #podeSerProduzida()}.
     *
     * @throws IllegalStateException Se a demanda não puder ser produzida.
     */
    public void iniciarProducao() {
        if (!podeSerProduzida()) {
            throw new IllegalStateException("A demanda de " + tipoProduto + " não pode entrar em produção (status: "
                    + status.getDescricao() + ", unidades: " + quantidadeProdutos + ").");
        }
        this.status = StatusDemanda.EM_PRODUCAO;
    }

    /**
     * Registra que uma unidade saiu da linha. Enquanto ainda houver unidades,
     * a demanda volta para {@code PENDENTE}, para que a estratégia consiga
     * selecioná-la no próximo ciclo; na última unidade ela fica {@code CONCLUIDA}.
     * Não faz nada se a demanda não estiver {@code EM_PRODUCAO}.
     */
    public void registrarUnidadeProduzida() {
        if (this.status != StatusDemanda.EM_PRODUCAO) {
            return;
        }
        this.quantidadeProdutos--;
        if (this.quantidadeProdutos == 0) {
            atender();
        } else {
            this.status = StatusDemanda.PENDENTE;
        }
    }

    /**
     * Desfaz o início da produção ({@code EM_PRODUCAO -> PENDENTE}) quando a
     * unidade não chega a ser fabricada.
     */
    public void interromperProducao() {
        if (this.status == StatusDemanda.EM_PRODUCAO) {
            this.status = StatusDemanda.PENDENTE;
        }
    }

    /**
     * Cancela a demanda. Demandas já concluídas não são afetadas.
     */
    public void cancelar() {
        if (this.status != StatusDemanda.CONCLUIDA) {
            this.status = StatusDemanda.CANCELADA;
        }
    }

    /**
     * Marca a demanda como concluída. Demandas canceladas não são afetadas.
     */
    public void atender() {
        if (this.status != StatusDemanda.CANCELADA) {
            this.status = StatusDemanda.CONCLUIDA;
        }
    }

    /**
     * Retorna o nome do modelo encomendado.
     *
     * @return O nome do modelo encomendado.
     */
    public String getTipoProduto() {
        return tipoProduto;
    }

    /**
     * Retorna quantas unidades ainda faltam fabricar.
     *
     * @return Quantas unidades ainda faltam fabricar.
     */
    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    /**
     * Retorna o status atual da demanda.
     *
     * @return O status atual da demanda.
     */
    public StatusDemanda getStatus() {
        return status;
    }
}
