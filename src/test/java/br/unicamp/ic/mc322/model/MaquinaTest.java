package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Composição da máquina (Tarefa 5): uma parte em falha para a máquina inteira,
 * o reparo é feito em cascata e a restauração respeita as regras das partes.
 * Usa a litografia, que tem 4 sensores térmicos.
 */
class MaquinaTest {

    private Maquina litografia;

    @BeforeEach
    void criarMaquina() {
        litografia = new EstacaoLitografia();
    }

    @Test
    void maquinaNovaNasceSaudavelComTodasAsPartes() {
        assertEquals(100.0, litografia.getSaude());
        assertEquals(4, litografia.getQuantidadeSensores());
        assertFalse(litografia.estaQuebrada());
        assertNull(litografia.descreverAvaria());
        assertFalse(litografia.estaLigada(), "a máquina nasce desligada; quem a liga é a planta");
    }

    @Test
    void motorEmFalhaParaAMaquina() {
        litografia.restaurarMotor(100.0);

        assertTrue(litografia.estaQuebrada());
        assertTrue(litografia.descreverAvaria().contains("motor"));
    }

    @Test
    void camaraDespressurizadaParaAMaquina() {
        litografia.restaurarCamara(10.0, false);

        assertTrue(litografia.estaQuebrada());
        assertTrue(litografia.descreverAvaria().contains("câmara"));
    }

    @Test
    void maquinaSoParaComMaisDaMetadeDosSensoresInoperantes() {
        litografia.restaurarCalibracaoSensor(0, 0.0);
        litografia.restaurarCalibracaoSensor(1, 0.0);
        assertFalse(litografia.estaQuebrada(), "2 de 4 sensores não é mais da metade");

        litografia.restaurarCalibracaoSensor(2, 0.0);
        assertTrue(litografia.estaQuebrada(), "3 de 4 sensores inoperantes param a máquina");
    }

    @Test
    void desgasteChegaATodasAsPartes() {
        litografia.aplicarDesgaste(1.0);

        assertEquals(2.5, litografia.getDesgasteMotor(), 1e-9);
        assertEquals(98.2, litografia.getIntegridadeVedacoesCamara(), 1e-9);
        assertEquals(99.0, litografia.getCalibracaoSensor(3), 1e-9);
        assertTrue(litografia.getSaude() <= 100.0 && litografia.getSaude() >= 97.0,
                "a perda de saúde é aleatória, mas fica entre 0 e 3 pontos por ciclo");
    }

    @Test
    void repararRestauraMaquinaEPartesEmCascata() {
        litografia.restaurarEstado(10.0, true);
        litografia.restaurarMotor(120.0);
        litografia.restaurarCamara(5.0, false);
        litografia.restaurarCalibracaoSensor(0, 0.0);

        litografia.reparar();

        assertEquals(100.0, litografia.getSaude());
        assertEquals(0.0, litografia.getDesgasteMotor());
        assertTrue(litografia.isCamaraPressurizada());
        assertEquals(100.0, litografia.getCalibracaoSensor(0));
        assertFalse(litografia.estaQuebrada());
    }

    @Test
    void restauracaoRecusaEstadosImpossiveis() {
        assertThrows(IllegalArgumentException.class, () -> litografia.restaurarEstado(150.0, true));
        assertThrows(IllegalArgumentException.class, () -> litografia.restaurarMotor(-1.0));
        assertThrows(IllegalArgumentException.class, () -> litografia.restaurarCamara(10.0, true),
                "não existe vácuo com as vedações abaixo de 20%");
        assertThrows(IllegalArgumentException.class, () -> litografia.restaurarCalibracaoSensor(4, 50.0),
                "a litografia só tem os sensores 0 a 3");
    }
}
