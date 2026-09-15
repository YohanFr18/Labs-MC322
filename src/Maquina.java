import java.util.Random;

public abstract class Maquina {
    private String nome;
    private boolean ligada;
    private int capacidadeMaxima;
    private float probabilidadeFalha;
    private double custoOperacao;
    private Random randomNum;

    public Maquina(String nome, int capacidadeMaxima, float probabilidadeFalha, double custoOperacao) {
        this.nome = nome;
        ligada = false;
        this.capacidadeMaxima = capacidadeMaxima;
        this.probabilidadeFalha = probabilidadeFalha;
        this.custoOperacao = custoOperacao;
        randomNum = new Random();
    }

    protected boolean verificarFalha() {
        if (randomNum.nextFloat() <= probabilidadeFalha) {
            return true;
        }
        return false;
    }

    protected boolean verificarFalha(float probabilidadeCustomizada) {
        if (randomNum.nextFloat() <= probabilidadeCustomizada) {
            return true;
        }
        return false;
    }

    public abstract boolean processar(Produto produto);

    public abstract String getTipo();

    public void ligar() {
        ligada = true;
    }

    public void desligar() {
        ligada = false;
    }

    public String getNome() {
        return nome;

    }

    public double getCustoOperacao() {
        return custoOperacao;
    }

    public boolean estaLigada() {
        return ligada;
    }

    public float getProbabilidadeFalha() {
        return probabilidadeFalha;
    }
}
