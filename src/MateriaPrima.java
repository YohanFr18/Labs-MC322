public class MateriaPrima {
    private String id;
    private String nome;
    private int quantidade;
    private String unidade;
    private int quantidadeMinima;
    private int CustoPorUnidade;

    public MateriaPrima(
            String id, String nome, int quantidade, String unidade, int quantidadeMinima, int CustoPorUnidade) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
        this.CustoPorUnidade = CustoPorUnidade;
    }

    public boolean consumir(int demanda) {
        if (demanda <= 0 || demanda > quantidade) {
            return false;
        }
        quantidade -= demanda;
        return true;
    }

    public boolean adicionarEstoque(int quantidadeAdicionada) {
        if (quantidadeAdicionada <= 0) {
            return false;
        }
        quantidade += quantidadeAdicionada;
        return true;
    }

    public boolean verificarDisponibilidade(int demanda) {
        if (demanda <= 0) {
            return false;
        }
        // Verificamos a quantidadeMinima para garantir um "estoque de segurança"
        // ou seja, após a produção, ainda sobrará, pelo menos, a quantidadeMinima
        return (demanda <= quantidade && quantidade - demanda >= quantidadeMinima);
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public int CustoPorUnidade() {
        return CustoPorUnidade;
    }

    public int getQuantidadeMinima() {
        return quantidadeMinima;
    }
}
