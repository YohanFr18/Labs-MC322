import java.util.ArrayList;
import java.util.List;

/**
 * Coordena a planta: guarda as demandas, a linha de produção, o estoque de
 * wafer, o budget e o armazém de produtos fabricados, e executa as operações
 * de negócio (fabricar, comprar matéria-prima, manter máquinas).
 *
 * <p>As máquinas ficam numa {@link LinhaDeProducao}. O gerenciador conversa
 * apenas com a linha e com as máquinas, nunca com as partes internas delas.</p>
 *
 * <p>É o contexto do padrão Strategy: a {@link EstrategiaProducao} ativa
 * apenas escolhe qual demanda produzir; quem fabrica e movimenta o budget é
 * o gerenciador.</p>
 *
 * <p>Erros de regra de negócio são sinalizados com subclasses de
 * {@link PlantaException}, para que a interface com o usuário decida como
 * apresentá-los. Toda operação valida suas condições antes de alterar o
 * estado: se uma exceção é lançada, estoque, budget e máquinas permanecem
 * como estavam.</p>
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class GerenciadorProducao {
    /** Custo, em reais, para recalibrar uma máquina. */
    public static final double CUSTO_MANUTENCAO = 300.0;

    private ArrayList<Demanda> demandas;
    private ArrayList<Produto> produtosFabricados;
    private LinhaDeProducao linhaPrincipal;
    private MateriaPrima materiaPrima;
    private double budget;
    private Cenario cenarioAtivo;
    private EstrategiaProducao estrategiaAtual;

    /**
     * Monta a planta com uma linha de produção de três estações (litografia,
     * encapsulamento e metrologia) já ligadas e calibradas para o cenário escolhido.
     *
     * @param materiaPrima Estoque de wafer usado na produção.
     * @param cenario Cenário de operação, que define o budget inicial e os
     *                 multiplicadores de falha e desgaste.
     */
    public GerenciadorProducao(MateriaPrima materiaPrima, Cenario cenario) {
        this.materiaPrima = materiaPrima;
        this.cenarioAtivo = cenario;
        this.budget = cenario.getBudgetInicial();
        demandas = new ArrayList<>();
        produtosFabricados = new ArrayList<>();

        // As estações são criadas aqui e adicionadas à linha (agregação, não composição)
        linhaPrincipal = new LinhaDeProducao("Linha Principal");
        linhaPrincipal.adicionarMaquina(new EstacaoLitografia());
        linhaPrincipal.adicionarMaquina(new EstacaoEncapsulamento());
        linhaPrincipal.adicionarMaquina(new EstacaoMetrologia());

        for (Maquina m : linhaPrincipal.getMaquinas()) {
            m.ligar();
            m.setMultiplicadorFalhaCenario(cenario.getMultiplicadorFalha());
        }
    }

    /**
     * Retorna o nome do cenário de operação ativo.
     *
     * @return O nome do cenário de operação ativo.
     */
    public String getNomeCenarioAtivo() {
        return cenarioAtivo.getDescricao();
    }

    /**
     * Troca a estratégia usada para escolher a próxima demanda.
     *
     * @param estrategia Nova estratégia de produção.
     */
    public void setEstrategia(EstrategiaProducao estrategia) {
        this.estrategiaAtual = estrategia;
    }

    /**
     * Retorna o nome da estratégia ativa, ou "Nenhuma" se nenhuma foi definida.
     *
     * @return O nome da estratégia ativa, ou "Nenhuma" se nenhuma foi definida.
     */
    public String getNomeEstrategiaAtual() {
        return estrategiaAtual != null ? estrategiaAtual.getNomeEstrategia() : "Nenhuma";
    }

    /**
     * Retorna o budget disponível, em reais.
     *
     * @return O budget disponível, em reais.
     */
    public double getBudget() {
        return budget;
    }

    /**
     * Adiciona uma demanda à fila da planta.
     *
     * @param d Demanda a registrar.
     */
    public void registrarDemanda(Demanda d) {
        demandas.add(d);
    }

    /**
     * Redefine a quantidade encomendada de um modelo. Uma quantidade positiva
     * reativa a demanda, mesmo que ela tenha sido cancelada.
     *
     * @param modelo Nome do modelo (ex.: "Turing-X4").
     * @param novaQuantidade Nova quantidade de unidades.
     * @throws DemandaInvalidaException Se não existir demanda para o modelo
     *                                  ou se a quantidade for negativa.
     */
    public void atualizarDemanda(String modelo, int novaQuantidade) {
        Demanda d = buscarDemanda(modelo);
        d.atualizarQuantidade(novaQuantidade);
        System.out.println("[OK] Encomenda de " + modelo + " ajustada para " + novaQuantidade + " unidade(s).");
    }

    /**
     * Imprime cada demanda com as unidades restantes e o status.
     */
    public void listarDemandas() {
        for (Demanda d : demandas) {
            System.out.printf("- %-13s | Unidades restantes: %3d | Status: %s%n",
                    d.getTipoProduto(), d.getQuantidadeProdutos(), d.getStatus().getDescricao());
        }
    }

    /**
     * Imprime o armazém agrupado por lote (um lote por modelo), com a
     * quantidade de unidades aprovadas, rejeitadas e em risco de cada um.
     */
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

    /**
     * Imprime o relatório de auditoria da linha de produção (com máquinas e
     * partes, em cascata) e de todos os produtos.
     */
    public void gerarAuditoriaGeral() {
        ArrayList<Auditavel> componentes = new ArrayList<>();
        // A linha também é Auditavel: ela mesma repassa a auditoria às máquinas
        componentes.add(linhaPrincipal);
        for (Produto p : produtosFabricados) {
            componentes.add(p);
        }
        exibirRelatorioAuditoria("AUDITORIA GERAL DA PLANTA", componentes);
    }

    /**
     * Imprime a auditoria estrutural da linha: a linha, cada estação e as
     * partes internas de cada estação, em texto hierárquico e indentado.
     */
    public void exibirAuditoriaEstrutural() {
        System.out.println("=== AUDITORIA ESTRUTURAL (Linha > Estações > Componentes) ===");
        System.out.println(linhaPrincipal.gerarRelatorioDiagnostico());
    }

    /**
     * Retorna quantas estações a linha de produção tem.
     *
     * @return Quantidade de estações na linha.
     */
    public int getQuantidadeMaquinas() {
        return linhaPrincipal.getMaquinas().size();
    }

    /**
     * Imprime um resumo numerado das estações da linha: tipo, se está ligada,
     * saúde e estado.
     */
    public void listarMaquinas() {
        System.out.println(linhaPrincipal.getNome() + ":");
        int numero = 1;
        for (Maquina m : linhaPrincipal.getMaquinas()) {
            String estado;
            if (m.estaQuebrada()) {
                estado = "PARADA: " + m.descreverAvaria();
            } else if (m.precisaManutencao()) {
                estado = "Requer manutenção";
            } else {
                estado = "OK";
            }
            System.out.printf("%d - %-25s | %-13s | %-9s | Saúde: %5.1f%% | %s%n", numero, m.getNome(), m.getTipo(),
                    m.estaLigada() ? "Ligada" : "Desligada", m.getSaude(), estado);
            numero++;
        }
    }

    /**
     * Liga a estação se ela estiver desligada, ou desliga se estiver ligada.
     * Uma estação desligada bloqueia a fabricação até ser ligada de novo.
     *
     * @param numero Número da estação, como mostrado em {@link #listarMaquinas()} (começa em 1).
     * @throws IllegalArgumentException Se não existir estação com esse número.
     */
    public void alternarEstadoMaquina(int numero) {
        List<Maquina> maquinas = linhaPrincipal.getMaquinas();
        if (numero < 1 || numero > maquinas.size()) {
            throw new IllegalArgumentException("Não existe estação número " + numero + " na linha.");
        }
        Maquina m = maquinas.get(numero - 1);
        if (m.estaLigada()) {
            m.desligar();
            System.out.println("[LINHA] " + m.getNome() + " desligada.");
        } else {
            m.ligar();
            System.out.println("[LINHA] " + m.getNome() + " ligada.");
        }
    }

    /**
     * Imprime o relatório de auditoria apenas dos produtos fabricados.
     */
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

    /**
     * Faz a manutenção (saúde volta a 100% e as partes internas são reparadas)
     * de todas as máquinas que precisam dela, cobrando {@link #CUSTO_MANUTENCAO} por máquina.
     * A operação é tudo ou nada: se o budget não cobrir todas as máquinas,
     * nenhuma é recalibrada e nada é cobrado.
     *
     * @throws RecursoInsuficienteException Se o budget não cobrir a manutenção
     *                                      de todas as máquinas que precisam dela.
     */
    public void realizarManutencao() {
        ArrayList<Maquina> precisamManutencao = new ArrayList<>();
        for (Maquina m : linhaPrincipal.getMaquinas()) {
            if (m.precisaManutencao()) {
                precisamManutencao.add(m);
            }
        }
        if (precisamManutencao.isEmpty()) {
            System.out.println("Nenhuma máquina precisa de manutenção agora.");
            return;
        }

        double custoTotal = CUSTO_MANUTENCAO * precisamManutencao.size();
        if (custoTotal > budget) {
            throw new RecursoInsuficienteException(String.format(
                    "Budget insuficiente para a manutenção: %d máquina(s) custam R$ %.2f e o budget atual é R$ %.2f. "
                            + "Nenhuma máquina foi recalibrada.",
                    precisamManutencao.size(), custoTotal, budget));
        }

        for (Maquina m : precisamManutencao) {
            budget -= CUSTO_MANUTENCAO;
            m.reparar();
            System.out.println("[REPARO] " + m.getNome() + " revisada (saúde e componentes internos restaurados) e de volta à linha.");
        }
    }

    /**
     * Compra wafer de silício, debitando o custo do budget.
     *
     * @param quantidade Quantidade de matéria-prima a comprar.
     * @throws IllegalArgumentException Se {@code quantidade} não for positiva.
     * @throws RecursoInsuficienteException Se o budget não cobrir a compra.
     */
    public void comprarMateriaPrima(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade comprada deve ser positiva: " + quantidade);
        }
        int custo = (quantidade * materiaPrima.getCustoPorUnidade());
        if (custo > budget) {
            throw new RecursoInsuficienteException(String.format(
                    "Budget insuficiente: %d %s custam R$ %d e o budget atual é R$ %.2f.",
                    quantidade, materiaPrima.getUnidade(), custo, budget));
        }
        budget -= custo;
        materiaPrima.adicionarEstoque(quantidade);
        System.out.println("[OK] " + quantidade + " " + materiaPrima.getUnidade() + " de wafer entregues ao estoque.");
    }

    /**
     * Fabrica uma unidade da demanda do modelo informado, ignorando a
     * estratégia ativa.
     *
     * @param modelo Nome do modelo a fabricar.
     * @throws DemandaInvalidaException Se o modelo não tiver demanda ou se a
     *                                  demanda não estiver pendente.
     * @throws RecursoInsuficienteException Se faltar wafer ou budget
     *                                      (a demanda é cancelada).
     * @throws MaquinaIndisponivelException Se alguma máquina estiver quebrada
     *                                      (inclusive por uma parte interna em falha)
     *                                      ou desligada.
     */
    public void fabricarDemanda(String modelo) {
        fabricarUnidade(buscarDemanda(modelo));
    }

    /**
     * Pede à estratégia ativa a próxima demanda e fabrica uma unidade dela.
     *
     * @throws PlantaException Se nenhuma estratégia estiver definida.
     * @throws DemandaInvalidaException Se a estratégia não encontrar demanda elegível.
     * @throws RecursoInsuficienteException Se faltar wafer ou budget
     *                                      (a demanda é cancelada).
     * @throws MaquinaIndisponivelException Se alguma máquina estiver quebrada
     *                                      ou desligada.
     */
    public void executarProximaProducao() {
        if (estrategiaAtual == null) {
            throw new PlantaException("Nenhuma estratégia de produção definida. Escolha uma em Gerenciar estratégia.");
        }
        // A estratégia apenas escolhe a demanda, quem fabrica e mexe no budget é o gerenciador
        Demanda escolhida = estrategiaAtual.selecionarDemanda(demandas, budget);
        if (escolhida == null) {
            throw new DemandaInvalidaException("A estratégia " + estrategiaAtual.getNomeEstrategia()
                    + " não encontrou nenhuma demanda elegível. Defina quantidades em Demandas"
                    + " ou reative uma encomenda cancelada.");
        }
        System.out.println("[" + estrategiaAtual.getNomeEstrategia() + "] Próxima encomenda: " + escolhida.getTipoProduto());
        fabricarUnidade(escolhida);
    }

    private Demanda buscarDemanda(String modelo) {
        for (Demanda d : demandas) {
            if (d.getTipoProduto().equals(modelo)) {
                return d;
            }
        }
        throw new DemandaInvalidaException("Não existe demanda registrada para o modelo " + modelo + ".");
    }

    private Produto criarProduto(String modelo) {
        return switch (modelo) {
            case "Turing-X4" -> new TuringX4();
            case "Lovelace-X8" -> new LovelaceX8();
            case "Torvalds-X16" -> new TorvaldsX16();
            default -> throw new DemandaInvalidaException("Modelo de processador desconhecido: " + modelo + ".");
        };
    }

    // Fabrica uma unidade em duas fases: primeiro todas as validações, que não
    // alteram nada, e só depois as alterações de estado. Assim uma exceção nunca
    // deixa estoque, budget ou demanda modificados pela metade.
    private void fabricarUnidade(Demanda demanda) {
        // --- Fase 1: validações ---
        if (!demanda.podeSerProduzida()) {
            throw new DemandaInvalidaException("A encomenda de " + demanda.getTipoProduto()
                    + " não pode ser fabricada agora (status: " + demanda.getStatus().getDescricao()
                    + ", unidades restantes: " + demanda.getQuantidadeProdutos()
                    + "). Ajuste a quantidade em Demandas.");
        }

        Produto produto = criarProduto(demanda.getTipoProduto());
        int waferNecessario = produto.getQuantidadeMateriaPrimaPorUnidade();
        double custoTotal = calcularCustoProducao();

        // Falta de wafer ou de budget cancela a encomenda (regra da Tarefa 3), para que a
        // estratégia passe para a próxima em vez de escolher sempre a mesma encomenda inviável
        if (!materiaPrima.verificarDisponibilidade(waferNecessario)) {
            demanda.cancelar();
            throw new RecursoInsuficienteException(String.format(
                    "Wafer insuficiente para o %s: são necessários %d %s, há %d %s e a reserva mínima é de %d %s. "
                            + "A encomenda foi cancelada: compre silício e reative-a em Demandas.",
                    produto.getNome(), waferNecessario, materiaPrima.getUnidade(), materiaPrima.getQuantidade(),
                    materiaPrima.getUnidade(), materiaPrima.getQuantidadeMinima(), materiaPrima.getUnidade()));
        }
        if (custoTotal > budget) {
            demanda.cancelar();
            throw new RecursoInsuficienteException(String.format(
                    "Budget insuficiente para o %s: a produção custa R$ %.2f e o budget atual é R$ %.2f. "
                            + "A encomenda foi cancelada: reative-a em Demandas quando houver caixa.",
                    produto.getNome(), custoTotal, budget));
        }

        // Verificamos todas as máquinas antes de começar, para não deixar
        // um wafer processado pela metade caso alguma esteja indisponível
        for (Maquina m : linhaPrincipal.getMaquinas()) {
            if (m.estaQuebrada()) {
                throw new MaquinaIndisponivelException(m.getNome() + " está parada: " + m.descreverAvaria()
                        + ". Faça a manutenção em Auditoria e manutenção.");
            }
            if (!m.estaLigada()) {
                throw new MaquinaIndisponivelException(m.getNome()
                        + " está desligada. Ligue-a em Linha de produção.");
            }
        }

        // --- Fase 2: alterações de estado ---
        demanda.iniciarProducao();
        for (Maquina m : linhaPrincipal.getMaquinas()) {
            if (!m.processar(produto)) {
                // Não deveria acontecer depois das validações, mas se acontecer
                // devolvemos a demanda a PENDENTE antes de qualquer outra alteração
                demanda.interromperProducao();
                throw new MaquinaIndisponivelException(m.getNome() + " não conseguiu processar o "
                        + produto.getNome() + ". Nenhuma alteração foi feita no estoque ou no budget.");
            }
        }
        // O desgaste só é aplicado depois que todas as estações processaram a unidade
        for (Maquina m : linhaPrincipal.getMaquinas()) {
            m.aplicarDesgaste(cenarioAtivo.getMultiplicadorDesgaste());
        }

        budget -= custoTotal;
        materiaPrima.consumir(waferNecessario);
        produtosFabricados.add(produto);
        demanda.registrarUnidadeProduzida();

        if (produto.getStatus() == StatusProduto.REJEITADO) {
            System.out.println("[METROLOGIA] " + produto.getId() + " " + produto.getNome()
                    + " reprovado na inspeção: defeito detectado no die.");
        } else {
            System.out.println("[OK] " + produto.getId() + " " + produto.getNome() + " aprovado e enviado ao armazém.");
        }
    }

    private double calcularCustoProducao() {
        double total = 0;
        for (Maquina m : linhaPrincipal.getMaquinas()) {
            total += m.getCustoOperacao();
        }
        return total;
    }
}