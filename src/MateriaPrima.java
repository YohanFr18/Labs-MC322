public class MateriaPrima {
    private String id;
    private String nome;
    private int quantidade;
    private String unidade;
    private int quantidadeMinima;

    public MateriaPrima(
            String id, String nome, int quantidade, String unidade, int quantidadeMinima) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
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
}
