package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regras de estoque da matéria-prima: consumo, reserva mínima e validação na criação.
 */
class MateriaPrimaTest {

    private MateriaPrima wafer;

    // Cada teste recebe um estoque novo: nenhum depende do que outro alterou
    @BeforeEach
    void criarEstoque() {
        wafer = new MateriaPrima("SIW-001", "Wafer de Silício", 500, "mm2", 100, 10);
    }

    @Test
    void consumirRetiraDoEstoque() {
        wafer.consumir(120);

        assertEquals(380, wafer.getQuantidade());
    }

    @Test
    void consumirMaisQueOEstoqueLancaExcecaoENaoAlteraNada() {
        assertThrows(RecursoInsuficienteException.class, () -> wafer.consumir(501));

        assertEquals(500, wafer.getQuantidade(), "o estoque não pode ser alterado por uma operação que falhou");
    }

    @Test
    void consumirQuantidadeNaoPositivaLancaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> wafer.consumir(0));
        assertThrows(IllegalArgumentException.class, () -> wafer.consumir(-10));
    }

    @Test
    void disponibilidadeRespeitaAReservaMinima() {
        assertTrue(wafer.verificarDisponibilidade(400), "sobram exatamente 100, a reserva mínima");
        assertFalse(wafer.verificarDisponibilidade(401), "sobrariam 99, abaixo da reserva");
    }

    @Test
    void adicionarEstoqueSomaAQuantidade() {
        wafer.adicionarEstoque(250);

        assertEquals(750, wafer.getQuantidade());
    }

    @Test
    void construtorRecusaValoresForaDasRegras() {
        assertThrows(IllegalArgumentException.class,
                () -> new MateriaPrima("X", "Wafer", -1, "mm2", 100, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new MateriaPrima("X", "Wafer", 500, "mm2", -1, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new MateriaPrima("X", "Wafer", 500, "mm2", 100, 0));
    }
}
