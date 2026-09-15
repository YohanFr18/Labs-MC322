public class EstacaoEncapsulamento extends Maquina {
    public EstacaoEncapsulamento() {
        super("Estacao de Encapsulamento", 760, 0.08f, 50.0);
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
        return "Embalagem";
    }
}
