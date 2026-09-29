import java.util.ArrayList;

public class GerenciadorProducao {
    public static final double CUSTO_MANUTENCAO = 300.0;

    private ArrayList<Demanda> demandas;
    private ArrayList<Produto> produtosFabricados;
    private ArrayList<Maquina> maquinas;
    private MateriaPrima materiaPrima;
    private double budget;
    private Cenario cenarioAtivo;
    private EstrategiaProducao estrategiaAtual;

    public GerenciadorProducao(MateriaPrima materiaPrima, Cenario cenario) {
        this.materiaPrima = materiaPrima;
        this.cenarioAtivo = cenario;
        this.budget = cenario.getBudgetInicial();
        demandas = new ArrayList<>();
        produtosFabricados = new ArrayList<>();
        maquinas = new ArrayList<>();
        maquinas.add(new EstacaoLitografia());
        maquinas.add(new EstacaoEncapsulamento());
        maquinas.add(new EstacaoMetrologia());
        for (Maquina m : maquinas) {
            m.ligar();
            m.setMultiplicadorFalhaCenario(cenario.getMultiplicadorFalha());
        }
    }

    public String getNomeCenarioAtivo() {
        return cenarioAtivo.getDescricao();
    }

    public void setEstrategia(EstrategiaProducao estrategia) {
        this.estrategiaAtual = estrategia;
    }

    public String getNomeEstrategiaAtual() {
        return estrategiaAtual != null ? estrategiaAtual.getNomeEstrategia() : "Nenhuma";
    }

    public double getBudget() {
        return budget;
    }

    public void registrarDemanda(Demanda d) {
        demandas.add(d);
    }

    public void atualizarDemanda(String modelo, int novaQuantidade) {
        for (Demanda d : demandas) {
            if (d.getTipoProduto().equals(modelo)) {
                if (d.atualizarQuantidade(novaQuantidade)) {
                    System.out.println("[OK] Encomenda de " + modelo + " ajustada para " + novaQuantidade + " unidade(s).");
                } else {
                    System.out.println("[FALHA] Quantidade inválida.");
                }
                return;
            }
        }
    }

    public void listarDemandas() {
        for (Demanda d : demandas) {
            System.out.printf("- %-13s | Unidades restantes: %3d | Status: %s%n",
                    d.getTipoProduto(), d.getQuantidadeProdutos(), d.getStatus().getDescricao());
        }
    }

    public void exibirArmazem() {
        if (produtosFabricados.isEmpty()) {
            System.out.println("Armazém vazio: nenhum processador saiu da linha ainda.");
            return;
        }

        // Cada modelo de processador forma um lote de produção
        ArrayList<String> lotes = new ArrayList<>();
        for (Produto p : produtosFabricados) {
            if (!lotes.contains(p.getNome())) {
                lotes.add(p.getNome());
            }
        }

        for (String lote : lotes) {
            int quantidade = 0;
            int aprovados = 0;
            int rejeitados = 0;
            int emRisco = 0;
            float riscoTotal = 0;
            float qualidade = 0;
            String tipo = "";
            for (Produto p : produtosFabricados) {
                if (!p.getNome().equals(lote)) {
                    continue;
                }
                quantidade++;
                qualidade = p.getQualidade();
                tipo = p.getTipo();
                if (p.getStatus() == StatusProduto.REJEITADO) {
                    rejeitados++;
                } else {
                    aprovados++;
                }
                if (p.precisaManutencao()) {
                    emRisco++;
                }
                riscoTotal += p.getProbabilidadeFalhaAcumulada();
            }
            System.out.printf("- Lote %s (%s)%n", lote, tipo);
            System.out.printf("    Qtd: %d | Aprovados: %d | Rejeitados: %d | Qualidade: %.2f | Risco médio: %.2f | Em risco: %d%n",
                    quantidade, aprovados, rejeitados, qualidade, riscoTotal / quantidade, emRisco);
        }
    }

    public void gerarAuditoriaGeral() {
        ArrayList<Auditavel> componentes = new ArrayList<>();
        for (Maquina m : maquinas) {
            componentes.add(m);
        }
        for (Produto p : produtosFabricados) {
            componentes.add(p);
        }
        exibirRelatorioAuditoria("AUDITORIA GERAL DA PLANTA", componentes);
    }

    public void detalharMaquinas() {
        ArrayList<Auditavel> componentes = new ArrayList<>();
        for (Maquina m : maquinas) {
            componentes.add(m);
        }
        exibirRelatorioAuditoria("AUDITORIA DAS MÁQUINAS", componentes);
    }

    public void detalharProdutos() {
        ArrayList<Auditavel> componentes = new ArrayList<>();
        for (Produto p : produtosFabricados) {
            componentes.add(p);
        }
        exibirRelatorioAuditoria("AUDITORIA DOS PRODUTOS", componentes);
    }

    // Trabalhamos apenas com a interface Auditavel, então o relatório
    // não precisa saber se o componente é uma máquina ou um produto
    private void exibirRelatorioAuditoria(String titulo, ArrayList<Auditavel> componentes) {
        System.out.println("=== " + titulo + " ===");
        if (componentes.isEmpty()) {
            System.out.println("Nada a auditar.");
            return;
        }

        int criticos = 0;
        for (Auditavel componente : componentes) {
            if (componente.precisaManutencao()) {
                criticos++;
                System.out.println("[ALERTA] " + componente.gerarRelatorioDiagnostico());
            } else {
                System.out.println("[OK]     " + componente.gerarRelatorioDiagnostico());
            }
        }
        System.out.println("Componentes auditados: " + componentes.size() + " | Em estado crítico: " + criticos);
    }

    public void realizarManutencao() {
        boolean algumaPrecisava = false;
        for (Maquina m : maquinas) {
            if (!m.precisaManutencao()) {
                continue;
            }
            algumaPrecisava = true;
            if (budget < CUSTO_MANUTENCAO) {
                System.out.printf("[FALHA] Budget insuficiente para recalibrar %s (R$ %.2f).%n", m.getNome(), CUSTO_MANUTENCAO);
                continue;
            }
            budget -= CUSTO_MANUTENCAO;
            m.setSaude(100.0);
            System.out.println("[REPARO] " + m.getNome() + " recalibrada e de volta à linha.");
        }
        if (!algumaPrecisava) {
            System.out.println("Nenhuma máquina precisa de manutenção agora.");
        }
    }

    public void comprarMateriaPrima(int quantidade) {
        if (quantidade <= 0) {
            System.out.println("[FALHA] Quantidade inválida.");
            return;
        }
        int custo = (quantidade * materiaPrima.getCustoPorUnidade());
        if (custo > budget) {
            System.out.printf("[FALHA] Budget insuficiente: a compra custa R$ %d.%n", custo);
            return;
        }
        budget -= custo;
        materiaPrima.adicionarEstoque(quantidade);
        System.out.println("[OK] " + quantidade + " " + materiaPrima.getUnidade() + " de wafer entregues ao estoque.");
    }

    public void fabricarDemanda(String modelo) {
        for (Demanda d : demandas) {
            if (d.getTipoProduto().equals(modelo)) {
                fabricarUnidade(d);
                return;
            }
        }
    }

    public void executarProximaProducao() {
        if (estrategiaAtual == null) {
            System.out.println("[FALHA] Nenhuma estratégia de produção definida.");
            return;
        }
        // A estratégia apenas escolhe a demanda, quem fabrica e mexe no budget é o gerenciador
        Demanda escolhida = estrategiaAtual.selecionarDemanda(demandas, budget);
        if (escolhida == null) {
            System.out.println("[" + estrategiaAtual.getNomeEstrategia() + "] Nenhuma demanda elegível no momento.");
            return;
        }
        System.out.println("[" + estrategiaAtual.getNomeEstrategia() + "] Próxima encomenda: " + escolhida.getTipoProduto());
        fabricarUnidade(escolhida);
    }

    private boolean fabricarUnidade(Demanda demanda) {
        if (!demanda.iniciarProducao()) {
            System.out.println("[AVISO] A encomenda de " + demanda.getTipoProduto() + " não está pendente ("
                    + demanda.getStatus().getDescricao() + ") ou não tem unidades a fabricar.");
            return false;
        }

        Produto produto = switch (demanda.getTipoProduto()) {
            case "Turing-X4" -> new TuringX4();
            case "Lovelace-X8" -> new LovelaceX8();
            case "Torvalds-X16" -> new TorvaldsX16();
            default -> null;
        };
        if (produto == null) {
            demanda.interromperProducao();
            return false;
        }

        if (!materiaPrima.verificarDisponibilidade(produto.getQuantidadeMateriaPrimaPorUnidade())) {
            demanda.cancelar();
            System.out.println("[CANCELADA] Wafer insuficiente para o " + produto.getNome()
                    + ". Compre silício e reative a encomenda em Demandas.");
            return false;
        }
        double custoTotal = calcularCustoProducao();
        if (custoTotal > budget) {
            demanda.cancelar();
            System.out.println("[CANCELADA] Budget insuficiente para o " + produto.getNome()
                    + ". Reative a encomenda em Demandas quando houver caixa.");
            return false;
        }

        // Verificamos todas as máquinas antes de começar, para não deixar
        // um wafer processado pela metade caso alguma esteja quebrada
        for (Maquina m : maquinas) {
            if (m.estaQuebrada()) {
                demanda.interromperProducao();
                System.out.println("[PARADA] " + m.getNome() + " está quebrada. Faça a manutenção no menu de Auditoria.");
                return false;
            }
        }

        for (Maquina m : maquinas) {
            if (!m.processar(produto)) {
                demanda.interromperProducao();
                return false;
            }
            m.aplicarDesgaste(cenarioAtivo.getMultiplicadorDesgaste());
        }

        budget -= custoTotal;
        materiaPrima.consumir(produto.getQuantidadeMateriaPrimaPorUnidade());
        produtosFabricados.add(produto);
        demanda.registrarUnidadeProduzida();

        if (produto.getStatus() == StatusProduto.REJEITADO) {
            System.out.println("[METROLOGIA] " + produto.getId() + " " + produto.getNome()
                    + " reprovado na inspeção: defeito detectado no die.");
        } else {
            System.out.println("[OK] " + produto.getId() + " " + produto.getNome() + " aprovado e enviado ao armazém.");
        }
        return true;
    }

    private double calcularCustoProducao() {
        double total = 0;
        for (Maquina m : maquinas) {
            total += m.getCustoOperacao();
        }
        return total;
    }
}
