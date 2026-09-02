public class Maquina {
    private String nome;
    private boolean ligada;
    private int capacidadeMaxima;

    public Maquina(String nome, int capacidadeMaxima) {
        this.nome = nome;
        ligada = false;
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public void ligar() {
        ligada = true;
    }

    public void desligar() {
        ligada = false;
    }

    public boolean processar(MateriaPrima materiaPrima, Produto produto, int demanda) {
        if (!ligada) {
            return false;
        }
        if (materiaPrima == null || produto == null) {
            return false;
        }
        if (demanda > capacidadeMaxima) {
            return false;
        }
        // Valida o produto antes de consumir a matéria-prima, para não gastar
        // estoque num produto que já foi processado/inspecionado
        if (produto.getStatus() != StatusProduto.AGUARDANDO_PROCESSAMENTO) {
            return false;
        }
        if (!materiaPrima.verificarDisponibilidade(demanda)) {
            return false;
        }

        // Consome a matéria-prima do estoque e transforma o produto
        if (!materiaPrima.consumir(demanda)) {
            return false;
        }
        return produto.processar(materiaPrima);
    }

    public String getNome() {
        return nome;

    }

    public boolean estaLigada() {
        return ligada;
    }
}
