public class Produto {
    private String id;
    private String nome;
    private StatusProduto status;
    private int quantidadeMateriaPrimaNecessaria;
    // Atributo materiaPrima criado para permitir rastrear a materia-prima
    // utilizada em cada tipo de Produto
    private MateriaPrima materiaPrima;

    // No construtor, nao recebemos status de fora (todos Produto começam com
    // AGUARDANDO_PROCESSAMENTO). materiaPrima também não é atribuída aqui, apenas
    // durante o processamento (processar), para garantir rastreabilidade do tipo
    // de materia-prima utilizada em cada produto
    public Produto(
            String id, String nome, int quantidadeMateriaPrimaNecessaria) {
        this.id = id;
        this.nome = nome;
        this.status = StatusProduto.AGUARDANDO_PROCESSAMENTO;
        this.quantidadeMateriaPrimaNecessaria = quantidadeMateriaPrimaNecessaria;
    }

    public boolean processar(MateriaPrima materiaPrima) {
        if (status == StatusProduto.AGUARDANDO_PROCESSAMENTO && materiaPrima != null) {
            this.materiaPrima = materiaPrima;
            status = StatusProduto.PROCESSADO;
            return true;
        }
        return false;
    }

    public boolean definirDemandaMateriaPrima(int demanda) {
        if (demanda <= 0) {
            return false;
        }
        quantidadeMateriaPrimaNecessaria = demanda;
        return true;

    }

    public int getDemandaMateriaPrima() {
        return quantidadeMateriaPrimaNecessaria;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public StatusProduto getStatus() {
        return status;
    }

    public MateriaPrima getMateriaPrima() {
        return materiaPrima;
    }

}
