public class Esteira {
    // Item como Object e Main.java faz casting, apenas durante a Tarefa 1
    // Um item pode ser um objeto MateriaPrima ou um objeto Produto
    // Sem interface comum ainda, o melhor é generalizar o type de item para Object
    // e lidar com isso na Main.java
    private Object item;
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

    public boolean adicionarItem(Object novoItem, int demanda) {
        // Item parado não é transportado
        if (!emMovimento) {
            return false;
        }

        if (item != null) {
            return false;
        }

        if (!verificarCapacidade(demanda)) {
            return false;
        }

        item = novoItem;
        return true;
    }

    public Object removerItem() {
        if (item == null) {
            return null;
        }

        Object itemRemovido = item;
        item = null;
        return itemRemovido;
    }

    public Object getItem() {
        return item;
    }

    public boolean isEmMovimento() {
        return emMovimento;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
}
