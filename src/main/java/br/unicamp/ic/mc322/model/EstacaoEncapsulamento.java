package br.unicamp.ic.mc322.model;

/**
 * Máquina responsável pela etapa de encapsulamento (embalagem) dos processadores na linha de montagem.
 * Esta estação sela o chip no seu invólucro final, adicionando risco ao produto em caso de falha.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class EstacaoEncapsulamento extends Maquina {
    
    /**
     * Construtor da Estação de Encapsulamento.
     * Inicializa a máquina com o seu nome, capacidade, probabilidade de falha base, custo de operação padrão
     * e 2 sensores térmicos (um na selagem e outro na cura do invólucro).
     */
    public EstacaoEncapsulamento() {
        super("Estacao de Encapsulamento", 760, 0.08f, 50.0, 2);
    }

    /**
     * Executa o processo de encapsulamento no produto fornecido.
     * O produto apenas pode ser encapsulado se já estiver no estado PROCESSADO.
     * Em caso de falha mecânica ou operacional da máquina, o produto sofre um incremento na sua probabilidade de falha acumulada.
     *
     * @param produto Instância do produto a ser encapsulado.
     * @return {@code true} se a operação for concluída (mesmo com inserção de falhas no produto), 
     *         {@code false} se a máquina estiver desligada ou o estado do produto for inválido.
     */
    @Override
    public boolean processar(Produto produto) {
        if (!estaLigada()) {
            return false;
        }

        if (produto.getStatus() != StatusProduto.PROCESSADO) {
            return false;
        }

        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha(getProbabilidadeFalha());
        }

        return true;
    }

    /**
     * Retorna o tipo funcional desta máquina.
     *
     * @return Uma string indicando que esta é uma máquina de "Embalagem".
     */
    @Override
    public String getTipo() {
        return "Embalagem";
    }
}