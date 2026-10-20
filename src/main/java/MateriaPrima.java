/**
 * Insumo consumido pela linha de produção (no projeto, o wafer de silício).
 * Controla o estoque disponível e mantém uma reserva mínima de segurança:
 * uma produção só é liberada se, depois dela, ainda sobrar pelo menos a
 * {@code quantidadeMinima} em estoque.
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class MateriaPrima {
    private String id;
    private String nome;
    private int quantidade;
    private String unidade;
    private int quantidadeMinima;
    private int custoPorUnidade;

    /**
     * Cria uma matéria-prima com estoque inicial.
     *
     * @param id Identificador do insumo (ex.: "SIW-001").
     * @param nome Nome legível do insumo.
     * @param quantidade Quantidade inicial em estoque.
     * @param unidade Unidade de medida do estoque (ex.: "mm2").
     * @param quantidadeMinima Reserva de segurança que nenhuma produção pode consumir.
     * @param custoPorUnidade Preço de compra de cada unidade, em reais.
     */
    public MateriaPrima(
            String id, String nome, int quantidade, String unidade, int quantidadeMinima, int custoPorUnidade) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
        this.custoPorUnidade = custoPorUnidade;
    }

    /**
     * Retira a quantidade informada do estoque. O estoque só é alterado se a
     * operação puder ser concluída por inteiro.
     *
     * @param demanda Quantidade a consumir.
     * @throws IllegalArgumentException Se {@code demanda} não for positiva.
     * @throws RecursoInsuficienteException Se não houver estoque suficiente.
     */
    public void consumir(int demanda) {
        if (demanda <= 0) {
            throw new IllegalArgumentException("A quantidade a consumir deve ser positiva: " + demanda);
        }
        if (demanda > quantidade) {
            throw new RecursoInsuficienteException("Estoque de " + nome + " insuficiente: são necessários "
                    + demanda + " " + unidade + " e há apenas " + quantidade + " " + unidade + ".");
        }
        quantidade -= demanda;
    }

    /**
     * Adiciona unidades ao estoque (por exemplo, após uma compra).
     *
     * @param quantidadeAdicionada Quantidade que entra no estoque.
     * @throws IllegalArgumentException Se {@code quantidadeAdicionada} não for positiva.
     */
    public void adicionarEstoque(int quantidadeAdicionada) {
        if (quantidadeAdicionada <= 0) {
            throw new IllegalArgumentException("A quantidade adicionada deve ser positiva: " + quantidadeAdicionada);
        }
        quantidade += quantidadeAdicionada;
    }

    /**
     * Verifica, sem alterar o estoque, se é possível consumir a quantidade
     * informada preservando a reserva mínima de segurança.
     *
     * @param demanda Quantidade que se pretende consumir.
     * @return {@code true} se, após o consumo, ainda restar pelo menos a
     *         quantidade mínima; {@code false} caso contrário ou se
     *         {@code demanda} não for positiva.
     */
    public boolean verificarDisponibilidade(int demanda) {
        if (demanda <= 0) {
            return false;
        }
        // Verificamos a quantidadeMinima para garantir um "estoque de segurança"
        // ou seja, após a produção, ainda sobrará, pelo menos, a quantidadeMinima
        return (demanda <= quantidade && quantidade - demanda >= quantidadeMinima);
    }

    /**
     * Retorna o identificador do insumo.
     *
     * @return O identificador do insumo.
     */
    public String getId() {
        return id;
    }

    /**
     * Retorna o nome legível do insumo.
     *
     * @return O nome legível do insumo.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna a quantidade atual em estoque.
     *
     * @return A quantidade atual em estoque.
     */
    public int getQuantidade() {
        return quantidade;
    }

    /**
     * Retorna a unidade de medida do estoque.
     *
     * @return A unidade de medida do estoque.
     */
    public String getUnidade() {
        return unidade;
    }

    /**
     * Retorna o preço de compra de cada unidade, em reais.
     *
     * @return O preço de compra de cada unidade, em reais.
     */
    public int getCustoPorUnidade() {
        return custoPorUnidade;
    }

    /**
     * Retorna a reserva de segurança que nenhuma produção pode consumir.
     *
     * @return A reserva de segurança que nenhuma produção pode consumir.
     */
    public int getQuantidadeMinima() {
        return quantidadeMinima;
    }
}
