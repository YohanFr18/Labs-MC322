public class Produto {
    private String id;
    private String nome;
    private StatusProduto status;
    private int quantidadeMateriaPrimaNecessaria;
    // Atributo materiaPrima criado para permitir rastrear a materia-prima
    // utilizada em cada tipo de Produto
    private MateriaPrima materiaPrima;

    public Produto(
            String id, String nome, int quantidadeMateriaPrimaNecessaria) {
        this.id = id;
        this.nome = nome;
        this.status = StatusProduto.AGUARDANDO_PROCESSAMENTO;
        this.quantidadeMateriaPrimaNecessaria = quantidadeMateriaPrimaNecessaria;
    }

}
