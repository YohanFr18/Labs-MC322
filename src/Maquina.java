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
        if (demanda > capacidadeMaxima) {
            return false;
        }

        // Matéria-prima é gasta do estoque
        if (!materiaPrima.verificarDisponibilidade(demanda)) {
            return false;
        }
        // O produto vira PROCESSADO e registra a origem
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
