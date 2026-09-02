public class Esteira {
    private String item;
    private boolean emMovimento;
    private int capacidadeMaxima;

    // Construtor
    public Esteira(int capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
        this.item = null;
        this.emMovimento = false;
    }

    public void ligar() {
        emMovimento = true;
    }

    public void desligar() {
        emMovimento = false;
    }

    public boolean verificarCapacidade(int demanda) {
        return demanda <= capacidadeMaxima;
    }

    public boolean adicionarItem(String novoItem, int demanda) {
        if (item != null) {
            return false;
        }

        if (!verificarCapacidade(demanda)) {
            return false;
        }

        item = novoItem;
        return true;
    }

    public String removerItem() {
        if (item == null) {
            return null;
        }

        String itemRemovido = item;
        item = null;
        return itemRemovido;
    }

    public String getItem() {
        return item;
    }

    public boolean isEmMovimento() {
        return emMovimento;
    }

    public double getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
}
