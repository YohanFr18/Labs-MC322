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
}
