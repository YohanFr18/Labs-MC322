package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import org.junit.jupiter.api.Test;

/**
 * Ciclo de vida da demanda (PENDENTE, EM_PRODUCAO, CONCLUIDA, CANCELADA) e
 * restauração a partir de dados salvos.
 */
class DemandaTest {

    @Test
    void novaDemandaComecaPendenteEQuantidadeNegativaViraZero() {
        Demanda demanda = new Demanda("Turing-X4", -3);

        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
        assertEquals(0, demanda.getQuantidadeProdutos());
        assertFalse(demanda.podeSerProduzida(), "sem unidades, não há o que produzir");
    }

    @Test
    void unidadeProduzidaVoltaParaPendenteEnquantoFaltamUnidades() {
        Demanda demanda = new Demanda("Turing-X4", 2);

        demanda.iniciarProducao();
        assertEquals(StatusDemanda.EM_PRODUCAO, demanda.getStatus());

        demanda.registrarUnidadeProduzida();
        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
        assertEquals(1, demanda.getQuantidadeProdutos());
    }

    @Test
    void ultimaUnidadeConcluiADemanda() {
        Demanda demanda = new Demanda("Lovelace-X8", 1);

        demanda.iniciarProducao();
        demanda.registrarUnidadeProduzida();

        assertEquals(StatusDemanda.CONCLUIDA, demanda.getStatus());
        assertEquals(0, demanda.getQuantidadeProdutos());
    }

    @Test
    void demandaCanceladaNaoEntraEmProducao() {
        Demanda demanda = new Demanda("Torvalds-X16", 5);
        demanda.cancelar();

        assertThrows(IllegalStateException.class, demanda::iniciarProducao);
        assertEquals(StatusDemanda.CANCELADA, demanda.getStatus());
    }

    @Test
    void atualizarQuantidadePositivaReativaDemandaCancelada() {
        Demanda demanda = new Demanda("Turing-X4", 5);
        demanda.cancelar();

        demanda.atualizarQuantidade(3);

        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
        assertTrue(demanda.podeSerProduzida());
    }

    @Test
    void atualizarQuantidadeNegativaLancaDemandaInvalida() {
        Demanda demanda = new Demanda("Turing-X4", 5);

        assertThrows(DemandaInvalidaException.class, () -> demanda.atualizarQuantidade(-1));
        assertEquals(5, demanda.getQuantidadeProdutos());
    }

    @Test
    void restaurarMantemQuantidadeEStatus() {
        Demanda demanda = Demanda.restaurar("Lovelace-X8", 4, StatusDemanda.CANCELADA);

        assertEquals("Lovelace-X8", demanda.getTipoProduto());
        assertEquals(4, demanda.getQuantidadeProdutos());
        assertEquals(StatusDemanda.CANCELADA, demanda.getStatus());
    }

    @Test
    void restaurarRecusaCombinacoesQueOCicloNuncaProduz() {
        assertThrows(DemandaInvalidaException.class,
                () -> Demanda.restaurar("Turing-X4", 2, StatusDemanda.EM_PRODUCAO));
        assertThrows(DemandaInvalidaException.class,
                () -> Demanda.restaurar("Turing-X4", 2, StatusDemanda.CONCLUIDA));
        assertThrows(DemandaInvalidaException.class,
                () -> Demanda.restaurar("Turing-X4", -1, StatusDemanda.PENDENTE));
    }
}
