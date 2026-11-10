package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Agregação da linha de produção: a coleção de máquinas só muda pelos métodos
 * da linha, e a linha pede manutenção se qualquer máquina pedir.
 */
class LinhaDeProducaoTest {

    @Test
    void listaDeMaquinasNaoPodeSerAlteradaDeFora() {
        LinhaDeProducao linha = new LinhaDeProducao("Linha Teste");
        linha.adicionarMaquina(new EstacaoLitografia());

        assertThrows(UnsupportedOperationException.class,
                () -> linha.getMaquinas().add(new EstacaoMetrologia()));
        assertEquals(1, linha.getMaquinas().size());
    }

    @Test
    void linhaPrecisaDeManutencaoSeAlgumaMaquinaPrecisar() {
        LinhaDeProducao linha = new LinhaDeProducao("Linha Teste");
        Maquina metrologia = new EstacaoMetrologia();
        linha.adicionarMaquina(new EstacaoLitografia());
        linha.adicionarMaquina(metrologia);
        assertFalse(linha.precisaManutencao());

        metrologia.restaurarEstado(20.0, true);

        assertTrue(linha.precisaManutencao());
    }
}
