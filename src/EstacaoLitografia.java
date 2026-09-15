public class EstacaoLitografia extends Maquina {
    public EstacaoLitografia() {
        super("Estacao de Litografia UV", 760, 0.15f, 75.0);
    }

    @Override
    public boolean processar(Produto produto) {
        if (!estaLigada()) {
            return false;
        }

        if (!produto.processar()) {
            return false;
        }

        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha(getProbabilidadeFalha());
        }

        return true;
    }

    @Override
    public String getTipo() {
        return "Processamento";
    }
}
