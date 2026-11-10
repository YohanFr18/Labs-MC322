package br.unicamp.ic.mc322.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.exception.PlantaException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.EstacaoLitografia;
import br.unicamp.ic.mc322.model.EstacaoMetrologia;
import br.unicamp.ic.mc322.model.LinhaDeProducao;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import org.junit.jupiter.api.Test;

/**
 * Regras de negócio do gerenciador: compra, fabricação, cancelamento por falta
 * de recursos, máquinas indisponíveis e manutenção "tudo ou nada".
 *
 * <p>As falhas sorteadas pelas máquinas só mudam o risco e a aprovação do
 * produto; por isso os testes conferem estoque, budget e demandas, que não
 * dependem de sorte.</p>
 */
class GerenciadorProducaoTest {

    private static final int WAFER_TURING = 120;

    private GerenciadorProducao novaPlanta(int estoqueWafer) {
        MateriaPrima wafer = new MateriaPrima("SIW-001", "Wafer de Silício", estoqueWafer, "mm2", 100, 10);
        GerenciadorProducao planta = new GerenciadorProducao(wafer, Cenario.IDEAL);
        planta.registrarDemanda(new Demanda("Turing-X4", 2));
        return planta;
    }

    private double custoDeUmaUnidade(GerenciadorProducao planta) {
        double custo = 0;
        for (Maquina m : planta.getLinhaPrincipal().getMaquinas()) {
            custo += m.getCustoOperacao();
        }
        return custo;
    }

    private Demanda demandaTuring(GerenciadorProducao planta) {
        return planta.getDemandas().get(0);
    }

    @Test
    void plantaNovaComecaComOBudgetDoCenarioEMaquinasLigadas() {
        GerenciadorProducao planta = novaPlanta(5000);

        assertEquals(Cenario.IDEAL.getBudgetInicial(), planta.getBudget());
        assertEquals(3, planta.getQuantidadeMaquinas());
        for (Maquina m : planta.getLinhaPrincipal().getMaquinas()) {
            assertTrue(m.estaLigada());
        }
    }

    @Test
    void comprarMateriaPrimaDebitaBudgetEAumentaEstoque() {
        GerenciadorProducao planta = novaPlanta(5000);

        planta.comprarMateriaPrima(100);

        assertEquals(Cenario.IDEAL.getBudgetInicial() - 1000.0, planta.getBudget());
        assertEquals(5100, planta.getMateriaPrima().getQuantidade());
    }

    @Test
    void compraAcimaDoBudgetFalhaSemAlterarNada() {
        GerenciadorProducao planta = novaPlanta(5000);
        double budgetAntes = planta.getBudget();

        assertThrows(RecursoInsuficienteException.class, () -> planta.comprarMateriaPrima(1_000_000));

        assertEquals(budgetAntes, planta.getBudget());
        assertEquals(5000, planta.getMateriaPrima().getQuantidade());
    }

    @Test
    void fabricarConsomeWaferBudgetEUmaUnidadeDaDemanda() {
        GerenciadorProducao planta = novaPlanta(5000);
        double budgetEsperado = planta.getBudget() - custoDeUmaUnidade(planta);

        planta.fabricarDemanda("Turing-X4");

        assertEquals(5000 - WAFER_TURING, planta.getMateriaPrima().getQuantidade());
        assertEquals(budgetEsperado, planta.getBudget(), 1e-9);
        assertEquals(1, demandaTuring(planta).getQuantidadeProdutos());
        assertEquals(StatusDemanda.PENDENTE, demandaTuring(planta).getStatus());
        assertEquals(1, planta.getQuantidadeProdutosFabricados());
    }

    @Test
    void fabricarAUltimaUnidadeConcluiADemanda() {
        GerenciadorProducao planta = novaPlanta(5000);

        planta.fabricarDemanda("Turing-X4");
        planta.fabricarDemanda("Turing-X4");

        assertEquals(StatusDemanda.CONCLUIDA, demandaTuring(planta).getStatus());
        assertThrows(DemandaInvalidaException.class, () -> planta.fabricarDemanda("Turing-X4"),
                "uma demanda concluída não pode ser fabricada de novo");
    }

    @Test
    void faltaDeWaferCancelaADemandaSemConsumirNada() {
        // 150 - 120 = 30, abaixo da reserva mínima de 100
        GerenciadorProducao planta = novaPlanta(150);
        double budgetAntes = planta.getBudget();

        assertThrows(RecursoInsuficienteException.class, () -> planta.fabricarDemanda("Turing-X4"));

        assertEquals(StatusDemanda.CANCELADA, demandaTuring(planta).getStatus());
        assertEquals(150, planta.getMateriaPrima().getQuantidade());
        assertEquals(budgetAntes, planta.getBudget());
        assertEquals(0, planta.getQuantidadeProdutosFabricados());
    }

    @Test
    void maquinaDesligadaImpedeAFabricacao() {
        GerenciadorProducao planta = novaPlanta(5000);
        planta.alternarEstadoMaquina(1);

        assertThrows(MaquinaIndisponivelException.class, () -> planta.fabricarDemanda("Turing-X4"));

        assertEquals(5000, planta.getMateriaPrima().getQuantidade());
        assertEquals(StatusDemanda.PENDENTE, demandaTuring(planta).getStatus(),
                "a demanda não pode ficar presa em EM_PRODUCAO");
    }

    @Test
    void parteInternaEmFalhaImpedeAFabricacao() {
        GerenciadorProducao planta = novaPlanta(5000);
        planta.getLinhaPrincipal().getMaquinas().get(0).restaurarMotor(100.0);

        MaquinaIndisponivelException erro = assertThrows(MaquinaIndisponivelException.class,
                () -> planta.fabricarDemanda("Turing-X4"));

        assertTrue(erro.getMessage().contains("motor"), "a mensagem deve dizer qual parte falhou");
    }

    @Test
    void manutencaoSemBudgetParaTodasNaoReparaNenhuma() {
        LinhaDeProducao linha = new LinhaDeProducao("Linha Teste");
        Maquina litografia = new EstacaoLitografia();
        Maquina metrologia = new EstacaoMetrologia();
        litografia.restaurarEstado(10.0, true);
        metrologia.restaurarEstado(10.0, true);
        linha.adicionarMaquina(litografia);
        linha.adicionarMaquina(metrologia);
        MateriaPrima wafer = new MateriaPrima("SIW-001", "Wafer", 5000, "mm2", 100, 10);
        // Reparar as duas custa 600; o budget só cobre uma
        GerenciadorProducao planta = new GerenciadorProducao(wafer, Cenario.IDEAL, 400.0, linha);

        assertThrows(RecursoInsuficienteException.class, planta::realizarManutencao);

        assertEquals(400.0, planta.getBudget());
        assertEquals(10.0, litografia.getSaude(), "nenhuma máquina pode ser reparada pela metade");
        assertEquals(10.0, metrologia.getSaude());
    }

    @Test
    void manutencaoReparaTodasAsMaquinasQuePrecisam() {
        GerenciadorProducao planta = novaPlanta(5000);
        Maquina litografia = planta.getLinhaPrincipal().getMaquinas().get(0);
        litografia.restaurarEstado(10.0, true);
        double budgetAntes = planta.getBudget();

        planta.realizarManutencao();

        assertEquals(100.0, litografia.getSaude());
        assertEquals(budgetAntes - GerenciadorProducao.CUSTO_MANUTENCAO, planta.getBudget());
    }

    @Test
    void produzirSemEstrategiaLancaPlantaException() {
        GerenciadorProducao planta = novaPlanta(5000);

        assertThrows(PlantaException.class, planta::executarProximaProducao);
    }

    @Test
    void modeloSemDemandaLancaDemandaInvalida() {
        GerenciadorProducao planta = novaPlanta(5000);

        assertThrows(DemandaInvalidaException.class, () -> planta.fabricarDemanda("Pentium-4"));
    }

    @Test
    void budgetNegativoNaoMontaPlanta() {
        MateriaPrima wafer = new MateriaPrima("SIW-001", "Wafer", 5000, "mm2", 100, 10);

        assertThrows(IllegalArgumentException.class,
                () -> new GerenciadorProducao(wafer, Cenario.IDEAL, -1.0, new LinhaDeProducao("L")));
    }
}
