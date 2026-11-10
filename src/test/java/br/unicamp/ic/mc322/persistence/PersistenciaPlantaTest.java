package br.unicamp.ic.mc322.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Salvar e carregar a planta em XML: ida e volta preservando estoque, máquinas
 * com suas partes e demandas, e reação a arquivos ausentes, vazios, malformados
 * ou com dados fora das regras do domínio.
 *
 * <p>Cada teste usa uma pasta temporária criada pelo JUnit ({@code @TempDir}) e
 * apagada no fim, então os testes nunca tocam o {@code dados/planta.xml} real
 * nem dependem uns dos outros.</p>
 */
class PersistenciaPlantaTest {

    @TempDir
    Path pastaTemporaria;

    private Path arquivo;
    private PersistenciaPlanta persistencia;

    @BeforeEach
    void prepararArquivo() {
        arquivo = pastaTemporaria.resolve("planta.xml");
        persistencia = new PersistenciaPlanta(arquivo);
    }

    // Planta com estado diferente do padrão em todos os pontos persistidos
    private GerenciadorProducao plantaComEstadoAlterado() {
        MateriaPrima wafer = new MateriaPrima("SIW-001", "Wafer de Silício", 5000, "mm2", 100, 10);
        GerenciadorProducao planta = new GerenciadorProducao(wafer, Cenario.APOCALIPTICO);
        planta.registrarDemanda(new Demanda("Turing-X4", 3));
        planta.registrarDemanda(new Demanda("Lovelace-X8", 2));
        planta.registrarDemanda(new Demanda("Torvalds-X16", 0));

        planta.fabricarDemanda("Turing-X4");
        planta.comprarMateriaPrima(50);
        planta.getDemandas().get(1).cancelar();

        List<Maquina> maquinas = planta.getLinhaPrincipal().getMaquinas();
        maquinas.get(0).restaurarMotor(42.5);
        maquinas.get(0).restaurarCamara(15.0, false);
        maquinas.get(0).restaurarCalibracaoSensor(2, 0.0);
        planta.alternarEstadoMaquina(2);
        return planta;
    }

    private void escrever(String conteudo) throws IOException {
        Files.writeString(arquivo, conteudo, StandardCharsets.UTF_8);
    }

    // Salva uma planta válida e troca um trecho do XML, simulando edição ou corrupção
    private void salvarETrocar(String trecho, String substituto) throws IOException {
        persistencia.salvar(plantaComEstadoAlterado());
        String xml = Files.readString(arquivo, StandardCharsets.UTF_8);
        assertTrue(xml.contains(trecho), "o XML gerado deveria conter " + trecho);
        escrever(xml.replace(trecho, substituto));
    }

    // ------------------------------------------------------------------
    // Salvar e carregar
    // ------------------------------------------------------------------

    @Test
    void salvarCriaOArquivo() {
        assertFalse(persistencia.existeArquivo());

        persistencia.salvar(plantaComEstadoAlterado());

        assertTrue(persistencia.existeArquivo());
    }

    @Test
    void salvarCriaAsPastasQueFaltam() {
        PersistenciaPlanta emSubpasta = new PersistenciaPlanta(pastaTemporaria.resolve("a/b/planta.xml"));

        emSubpasta.salvar(plantaComEstadoAlterado());

        assertTrue(emSubpasta.existeArquivo());
    }

    @Test
    void carregarRecuperaCenarioBudgetEEstoque() {
        GerenciadorProducao original = plantaComEstadoAlterado();
        persistencia.salvar(original);

        GerenciadorProducao carregada = persistencia.carregar();

        assertNotSame(original, carregada, "carregar deve montar uma planta nova");
        assertEquals(Cenario.APOCALIPTICO, carregada.getCenario());
        assertEquals(original.getBudget(), carregada.getBudget());
        MateriaPrima esperada = original.getMateriaPrima();
        MateriaPrima lida = carregada.getMateriaPrima();
        assertEquals(esperada.getId(), lida.getId());
        assertEquals(esperada.getNome(), lida.getNome());
        assertEquals(esperada.getQuantidade(), lida.getQuantidade());
        assertEquals(esperada.getUnidade(), lida.getUnidade());
        assertEquals(esperada.getQuantidadeMinima(), lida.getQuantidadeMinima());
        assertEquals(esperada.getCustoPorUnidade(), lida.getCustoPorUnidade());
    }

    @Test
    void carregarRecuperaMaquinasEPartesInternas() {
        GerenciadorProducao original = plantaComEstadoAlterado();
        persistencia.salvar(original);

        GerenciadorProducao carregada = persistencia.carregar();

        List<Maquina> esperadas = original.getLinhaPrincipal().getMaquinas();
        List<Maquina> lidas = carregada.getLinhaPrincipal().getMaquinas();
        assertEquals(original.getLinhaPrincipal().getNome(), carregada.getLinhaPrincipal().getNome());
        assertEquals(esperadas.size(), lidas.size());
        for (int i = 0; i < esperadas.size(); i++) {
            Maquina esperada = esperadas.get(i);
            Maquina lida = lidas.get(i);
            assertEquals(esperada.getClass(), lida.getClass(), "a ordem e o tipo das estações são mantidos");
            assertEquals(esperada.estaLigada(), lida.estaLigada());
            assertEquals(esperada.getSaude(), lida.getSaude());
            assertEquals(esperada.getDesgasteMotor(), lida.getDesgasteMotor());
            assertEquals(esperada.getIntegridadeVedacoesCamara(), lida.getIntegridadeVedacoesCamara());
            assertEquals(esperada.isCamaraPressurizada(), lida.isCamaraPressurizada());
            assertEquals(esperada.getQuantidadeSensores(), lida.getQuantidadeSensores());
            for (int s = 0; s < esperada.getQuantidadeSensores(); s++) {
                assertEquals(esperada.getCalibracaoSensor(s), lida.getCalibracaoSensor(s));
            }
            assertEquals(esperada.estaQuebrada(), lida.estaQuebrada());
        }
        assertFalse(lidas.get(1).estaLigada(), "a estação 2 foi desligada antes de salvar");
        assertTrue(lidas.get(0).estaQuebrada(), "a câmara despressurizada continua parando a litografia");
    }

    @Test
    void carregarRecuperaDemandasEStatus() {
        GerenciadorProducao original = plantaComEstadoAlterado();
        persistencia.salvar(original);

        GerenciadorProducao carregada = persistencia.carregar();

        List<Demanda> esperadas = original.getDemandas();
        List<Demanda> lidas = carregada.getDemandas();
        assertEquals(esperadas.size(), lidas.size());
        for (int i = 0; i < esperadas.size(); i++) {
            assertEquals(esperadas.get(i).getTipoProduto(), lidas.get(i).getTipoProduto());
            assertEquals(esperadas.get(i).getQuantidadeProdutos(), lidas.get(i).getQuantidadeProdutos());
            assertEquals(esperadas.get(i).getStatus(), lidas.get(i).getStatus());
        }
        assertEquals(StatusDemanda.CANCELADA, lidas.get(1).getStatus());
    }

    @Test
    void plantaCarregadaContinuaOperando() {
        persistencia.salvar(plantaComEstadoAlterado());
        GerenciadorProducao carregada = persistencia.carregar();
        // Liga a estação 2 e conserta a litografia para poder produzir
        carregada.alternarEstadoMaquina(2);
        carregada.realizarManutencao();
        int estoqueAntes = carregada.getMateriaPrima().getQuantidade();

        carregada.fabricarDemanda("Turing-X4");

        assertEquals(estoqueAntes - 120, carregada.getMateriaPrima().getQuantidade());
        assertEquals(1, carregada.getDemandas().get(0).getQuantidadeProdutos());
    }

    @Test
    void salvarDeNovoAtualizaOArquivo() {
        GerenciadorProducao planta = plantaComEstadoAlterado();
        persistencia.salvar(planta);

        planta.comprarMateriaPrima(10);
        planta.atualizarDemanda("Torvalds-X16", 7);
        persistencia.salvar(planta);
        GerenciadorProducao carregada = persistencia.carregar();

        assertEquals(planta.getMateriaPrima().getQuantidade(), carregada.getMateriaPrima().getQuantidade());
        assertEquals(planta.getBudget(), carregada.getBudget());
        assertEquals(7, carregada.getDemandas().get(2).getQuantidadeProdutos());
    }

    // ------------------------------------------------------------------
    // Situações de erro
    // ------------------------------------------------------------------

    @Test
    void arquivoInexistenteLancaPersistenciaException() {
        PersistenciaException erro = assertThrows(PersistenciaException.class, persistencia::carregar);

        assertTrue(erro.getMessage().contains("não encontrado"));
    }

    @Test
    void arquivoVazioLancaPersistenciaException() throws IOException {
        escrever("");

        PersistenciaException erro = assertThrows(PersistenciaException.class, persistencia::carregar);

        assertTrue(erro.getMessage().contains("vazio"));
    }

    @Test
    void xmlMalformadoLancaPersistenciaException() throws IOException {
        escrever("<planta><cenario>IDEAL</cenario>");

        PersistenciaException erro = assertThrows(PersistenciaException.class, persistencia::carregar);

        assertTrue(erro.getMessage().contains("não é um XML válido"));
    }

    @Test
    void xmlDeOutroFormatoLancaPersistenciaException() throws IOException {
        escrever("<?xml version=\"1.0\"?><fabrica><nome>Outra</nome></fabrica>");

        assertThrows(PersistenciaException.class, persistencia::carregar);
    }

    @Test
    void elementoObrigatorioAusenteLancaPersistenciaException() throws IOException {
        // Troca a abertura e o fechamento: o XML continua bem formado, mas sem <budget>
        salvarETrocar("budget>", "orcamento>");

        PersistenciaException erro = assertThrows(PersistenciaException.class, persistencia::carregar);

        assertTrue(erro.getMessage().contains("budget"), erro.getMessage());
    }

    @Test
    void numeroInvalidoLancaPersistenciaException() throws IOException {
        salvarETrocar("<custoPorUnidade>10<", "<custoPorUnidade>dez<");

        assertThrows(PersistenciaException.class, persistencia::carregar);
    }

    @Test
    void estoqueNegativoViolaRegraDoDominio() throws IOException {
        salvarETrocar("<quantidadeMinima>100<", "<quantidadeMinima>-100<");

        assertThrows(PersistenciaException.class, persistencia::carregar);
    }

    @Test
    void statusDesconhecidoLancaPersistenciaException() throws IOException {
        salvarETrocar("status=\"CANCELADA\"", "status=\"PAUSADA\"");

        assertThrows(PersistenciaException.class, persistencia::carregar);
    }

    @Test
    void demandaEmProducaoNaoPodeSerRestaurada() throws IOException {
        salvarETrocar("status=\"CANCELADA\"", "status=\"EM_PRODUCAO\"");

        assertThrows(PersistenciaException.class, persistencia::carregar);
    }

    @Test
    void quantidadeDeSensoresIncompativelComAEstacao() throws IOException {
        // Remove um dos sensores da primeira máquina (a litografia tem 4)
        salvarETrocar("<sensorTermico calibracao=\"0.0\"/>", "");

        PersistenciaException erro = assertThrows(PersistenciaException.class, persistencia::carregar);

        assertTrue(erro.getMessage().contains("sensores"), erro.getMessage());
    }

    @Test
    void falhaAoCarregarNaoAlteraAPlantaEmUso() throws IOException {
        persistencia.salvar(plantaComEstadoAlterado());
        GerenciadorProducao emUso = persistencia.carregar();
        double budgetAntes = emUso.getBudget();
        int estoqueAntes = emUso.getMateriaPrima().getQuantidade();

        escrever("lixo que não é XML");
        assertThrows(PersistenciaException.class, persistencia::carregar);

        assertEquals(budgetAntes, emUso.getBudget());
        assertEquals(estoqueAntes, emUso.getMateriaPrima().getQuantidade());
    }

    @Test
    void falhaDeEscritaLancaPersistenciaException() throws IOException {
        // Um arquivo comum no lugar da pasta impede a gravação
        Path bloqueio = pastaTemporaria.resolve("bloqueio");
        Files.writeString(bloqueio, "não sou uma pasta");
        PersistenciaPlanta impossivel = new PersistenciaPlanta(bloqueio.resolve("planta.xml"));

        assertThrows(PersistenciaException.class, () -> impossivel.salvar(plantaComEstadoAlterado()));
        assertEquals("não sou uma pasta", Files.readString(bloqueio));
    }
}
