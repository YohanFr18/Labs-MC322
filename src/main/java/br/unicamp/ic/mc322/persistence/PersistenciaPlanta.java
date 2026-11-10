package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.exception.PlantaException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.EstacaoEncapsulamento;
import br.unicamp.ic.mc322.model.EstacaoLitografia;
import br.unicamp.ic.mc322.model.EstacaoMetrologia;
import br.unicamp.ic.mc322.model.LinhaDeProducao;
import br.unicamp.ic.mc322.model.Maquina;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.service.GerenciadorProducao;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Salva e carrega o estado da planta em um arquivo XML.
 *
 * <p>É a única classe do sistema que conhece o formato do arquivo: as classes
 * de domínio não leem nem escrevem arquivos. O estado persistido é o cenário,
 * o budget, o estoque de matéria-prima, as máquinas da linha com suas partes
 * internas (motor, câmara de vácuo e sensores) e as demandas com seus status.</p>
 *
 * <p>{@link #carregar()} nunca altera uma planta existente: ele monta e valida
 * uma planta nova e só a devolve se tudo estiver correto. Assim, se o arquivo
 * estiver corrompido, quem chamou continua com a planta que já tinha.</p>
 *
 * <p>Todas as falhas (arquivo inexistente ou vazio, XML malformado, erro de
 * leitura/escrita, dado fora das regras do domínio) são sinalizadas com
 * {@link PersistenciaException}.</p>
 *
 * @author Victor Rimes e Yohan Rocha
 */
public class PersistenciaPlanta {

    private final Path arquivo;

    /**
     * Cria a persistência ligada a um arquivo. O arquivo não precisa existir
     * ainda: ele é criado no primeiro {@link #salvar(GerenciadorProducao)}.
     *
     * @param arquivo Caminho do arquivo XML de estado.
     * @throws IllegalArgumentException Se o caminho for nulo.
     */
    public PersistenciaPlanta(Path arquivo) {
        if (arquivo == null) {
            throw new IllegalArgumentException("O caminho do arquivo de estado é obrigatório.");
        }
        this.arquivo = arquivo;
    }

    /**
     * Retorna o caminho do arquivo de estado.
     *
     * @return O caminho do arquivo XML.
     */
    public Path getArquivo() {
        return arquivo;
    }

    /**
     * Informa se já existe um estado salvo.
     *
     * @return {@code true} se o arquivo de estado existir.
     */
    public boolean existeArquivo() {
        return Files.exists(arquivo);
    }

    // =====================================================================
    // Salvar
    // =====================================================================

    /**
     * Grava o estado atual da planta, substituindo o que já houver no arquivo.
     * A escrita é feita num arquivo temporário que só então toma o lugar do
     * original, para que uma falha no meio da gravação não corrompa o estado
     * salvo anteriormente.
     *
     * @param planta A planta a ser salva.
     * @throws IllegalArgumentException Se a planta for nula.
     * @throws PersistenciaException Se não for possível gravar o arquivo.
     */
    public void salvar(GerenciadorProducao planta) {
        if (planta == null) {
            throw new IllegalArgumentException("Não há planta para salvar.");
        }
        Document doc = novoDocumento();
        Element raiz = doc.createElement("planta");
        doc.appendChild(raiz);

        adicionarTexto(doc, raiz, "cenario", planta.getCenario().name());
        adicionarTexto(doc, raiz, "budget", Double.toString(planta.getBudget()));
        raiz.appendChild(escreverMateriaPrima(doc, planta.getMateriaPrima()));
        raiz.appendChild(escreverLinha(doc, planta.getLinhaPrincipal()));

        Element demandas = doc.createElement("demandas");
        for (Demanda d : planta.getDemandas()) {
            Element demanda = doc.createElement("demanda");
            demanda.setAttribute("modelo", d.getTipoProduto());
            demanda.setAttribute("quantidade", Integer.toString(d.getQuantidadeProdutos()));
            demanda.setAttribute("status", d.getStatus().name());
            demandas.appendChild(demanda);
        }
        raiz.appendChild(demandas);

        gravar(doc);
    }

    private Element escreverMateriaPrima(Document doc, MateriaPrima mp) {
        Element elemento = doc.createElement("materiaPrima");
        elemento.setAttribute("id", mp.getId());
        adicionarTexto(doc, elemento, "nome", mp.getNome());
        adicionarTexto(doc, elemento, "quantidade", Integer.toString(mp.getQuantidade()));
        adicionarTexto(doc, elemento, "unidade", mp.getUnidade());
        adicionarTexto(doc, elemento, "quantidadeMinima", Integer.toString(mp.getQuantidadeMinima()));
        adicionarTexto(doc, elemento, "custoPorUnidade", Integer.toString(mp.getCustoPorUnidade()));
        return elemento;
    }

    private Element escreverLinha(Document doc, LinhaDeProducao linha) {
        Element elemento = doc.createElement("linhaDeProducao");
        elemento.setAttribute("nome", linha.getNome());
        for (Maquina m : linha.getMaquinas()) {
            elemento.appendChild(escreverMaquina(doc, m));
        }
        return elemento;
    }

    // A máquina expõe o estado das partes por métodos de delegação, então a
    // persistência nunca recebe uma referência para o motor, a câmara ou os sensores
    private Element escreverMaquina(Document doc, Maquina m) {
        Element maquina = doc.createElement("maquina");
        maquina.setAttribute("tipo", codigoDaMaquina(m));
        maquina.setAttribute("ligada", Boolean.toString(m.estaLigada()));
        maquina.setAttribute("saude", Double.toString(m.getSaude()));

        Element motor = doc.createElement("motorEsteira");
        motor.setAttribute("desgaste", Double.toString(m.getDesgasteMotor()));
        maquina.appendChild(motor);

        Element camara = doc.createElement("camaraVaco");
        camara.setAttribute("integridadeVedacoes", Double.toString(m.getIntegridadeVedacoesCamara()));
        camara.setAttribute("pressurizada", Boolean.toString(m.isCamaraPressurizada()));
        maquina.appendChild(camara);

        Element sensores = doc.createElement("sensores");
        for (int i = 0; i < m.getQuantidadeSensores(); i++) {
            Element sensor = doc.createElement("sensorTermico");
            sensor.setAttribute("calibracao", Double.toString(m.getCalibracaoSensor(i)));
            sensores.appendChild(sensor);
        }
        maquina.appendChild(sensores);
        return maquina;
    }

    // O tipo da estação define a configuração fixa dela (nome, capacidade,
    // custo, probabilidade de falha e quantidade de sensores)
    private String codigoDaMaquina(Maquina m) {
        if (m instanceof EstacaoLitografia) {
            return "LITOGRAFIA";
        }
        if (m instanceof EstacaoEncapsulamento) {
            return "ENCAPSULAMENTO";
        }
        if (m instanceof EstacaoMetrologia) {
            return "METROLOGIA";
        }
        throw new PersistenciaException("Não há formato de arquivo para a máquina " + m.getNome() + ".");
    }

    private Maquina criarMaquina(String codigo) {
        return switch (codigo) {
            case "LITOGRAFIA" -> new EstacaoLitografia();
            case "ENCAPSULAMENTO" -> new EstacaoEncapsulamento();
            case "METROLOGIA" -> new EstacaoMetrologia();
            default -> throw new PersistenciaException("Tipo de máquina desconhecido no arquivo: '" + codigo + "'.");
        };
    }

    private void adicionarTexto(Document doc, Element pai, String nome, String valor) {
        Element filho = doc.createElement(nome);
        filho.setTextContent(valor);
        pai.appendChild(filho);
    }

    private Document novoDocumento() {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            doc.setXmlStandalone(true);
            return doc;
        } catch (ParserConfigurationException e) {
            throw new PersistenciaException("Erro interno ao preparar o XML: " + e.getMessage(), e);
        }
    }

    private void gravar(Document doc) {
        Path temporario = null;
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            Files.createDirectories(pasta);
            temporario = Files.createTempFile(pasta, "planta-", ".tmp");

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            // Escrevemos a declaração à mão: a do Transformer sai colada em <planta>
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            try (OutputStream saida = Files.newOutputStream(temporario)) {
                saida.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes(StandardCharsets.UTF_8));
                transformer.transform(new DOMSource(doc), new StreamResult(saida));
            }

            // Só agora o arquivo antigo é substituído, de uma vez
            try {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | TransformerException e) {
            throw new PersistenciaException("Não foi possível gravar o estado em " + arquivo + ": " + e.getMessage(), e);
        } finally {
            apagarTemporario(temporario);
        }
    }

    private void apagarTemporario(Path temporario) {
        if (temporario == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporario);
        } catch (IOException e) {
            // Sobra só um .tmp na pasta de dados; o estado salvo não é afetado
        }
    }

    // =====================================================================
    // Carregar
    // =====================================================================

    /**
     * Lê o arquivo e reconstrói uma planta nova com o estado salvo. Todos os
     * dados são validados antes de a planta ser devolvida; nenhuma planta já
     * existente é alterada.
     *
     * <p>A estratégia de produção e o armazém de produtos não fazem parte do
     * arquivo: a planta devolvida não tem estratégia definida e começa com o
     * armazém vazio.</p>
     *
     * @return A planta recuperada do arquivo.
     * @throws PersistenciaException Se o arquivo não existir, estiver vazio, não
     *                               for um XML válido, não puder ser lido ou
     *                               tiver dados incompatíveis com o domínio.
     */
    public GerenciadorProducao carregar() {
        if (!Files.exists(arquivo)) {
            throw new PersistenciaException("Arquivo de estado não encontrado: " + arquivo);
        }
        Document doc = lerDocumento();
        try {
            return montarPlanta(doc.getDocumentElement());
        } catch (PersistenciaException e) {
            throw e;
        } catch (PlantaException | IllegalArgumentException e) {
            // Regras de domínio violadas (ex.: estoque negativo, saúde acima de 100%)
            throw new PersistenciaException("Dados inválidos em " + arquivo + ": " + e.getMessage(), e);
        }
    }

    private Document lerDocumento() {
        try {
            if (Files.size(arquivo) == 0) {
                throw new PersistenciaException("O arquivo de estado " + arquivo + " está vazio.");
            }
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            // O arquivo pode ter sido editado à mão: recusamos DOCTYPE para não
            // processar entidades externas (ataque conhecido como XXE)
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder leitor = fabrica.newDocumentBuilder();
            // Sem isso o leitor imprime o erro no terminal além de lançar a exceção
            leitor.setErrorHandler(new DefaultHandler());
            return leitor.parse(arquivo.toFile());
        } catch (SAXException e) {
            throw new PersistenciaException("O arquivo " + arquivo + " não é um XML válido (corrompido?): "
                    + e.getMessage(), e);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler " + arquivo + ": " + e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new PersistenciaException("Erro interno ao preparar a leitura do XML: " + e.getMessage(), e);
        }
    }

    private GerenciadorProducao montarPlanta(Element raiz) {
        if (!raiz.getTagName().equals("planta")) {
            throw new PersistenciaException("O arquivo não é um estado da planta: o elemento raiz é <"
                    + raiz.getTagName() + ">, esperado <planta>.");
        }
        Cenario cenario = lerCenario(texto(raiz, "cenario"));
        double budget = lerDouble(texto(raiz, "budget"), "budget");
        MateriaPrima materiaPrima = lerMateriaPrima(filho(raiz, "materiaPrima"));
        LinhaDeProducao linha = lerLinha(filho(raiz, "linhaDeProducao"));

        GerenciadorProducao planta = new GerenciadorProducao(materiaPrima, cenario, budget, linha);

        List<String> modelosLidos = new ArrayList<>();
        for (Element e : filhos(filho(raiz, "demandas"), "demanda")) {
            String modelo = atributo(e, "modelo");
            if (!GerenciadorProducao.isModeloConhecido(modelo)) {
                throw new PersistenciaException("Demanda de modelo desconhecido: '" + modelo + "'.");
            }
            if (modelosLidos.contains(modelo)) {
                throw new PersistenciaException("Há mais de uma demanda para o modelo " + modelo + ".");
            }
            modelosLidos.add(modelo);
            int quantidade = lerInt(atributo(e, "quantidade"), "quantidade da demanda de " + modelo);
            StatusDemanda status = lerStatus(atributo(e, "status"));
            planta.registrarDemanda(Demanda.restaurar(modelo, quantidade, status));
        }
        return planta;
    }

    private MateriaPrima lerMateriaPrima(Element e) {
        return new MateriaPrima(
                atributo(e, "id"),
                texto(e, "nome"),
                lerInt(texto(e, "quantidade"), "quantidade da matéria-prima"),
                texto(e, "unidade"),
                lerInt(texto(e, "quantidadeMinima"), "quantidade mínima da matéria-prima"),
                lerInt(texto(e, "custoPorUnidade"), "custo por unidade da matéria-prima"));
    }

    private LinhaDeProducao lerLinha(Element e) {
        LinhaDeProducao linha = new LinhaDeProducao(atributo(e, "nome"));
        List<Element> maquinas = filhos(e, "maquina");
        if (maquinas.isEmpty()) {
            throw new PersistenciaException("A linha de produção do arquivo não tem nenhuma máquina.");
        }
        for (Element m : maquinas) {
            linha.adicionarMaquina(lerMaquina(m));
        }
        return linha;
    }

    // A máquina é criada pelo construtor normal (que cria as partes, como na
    // composição da Tarefa 5) e só depois recebe o estado salvo
    private Maquina lerMaquina(Element e) {
        String tipo = atributo(e, "tipo");
        Maquina maquina = criarMaquina(tipo);
        maquina.restaurarEstado(lerDouble(atributo(e, "saude"), "saúde da " + tipo),
                lerBoolean(atributo(e, "ligada"), "ligada da " + tipo));

        Element motor = filho(e, "motorEsteira");
        maquina.restaurarMotor(lerDouble(atributo(motor, "desgaste"), "desgaste do motor da " + tipo));

        Element camara = filho(e, "camaraVaco");
        maquina.restaurarCamara(
                lerDouble(atributo(camara, "integridadeVedacoes"), "vedações da câmara da " + tipo),
                lerBoolean(atributo(camara, "pressurizada"), "pressurizada da câmara da " + tipo));

        List<Element> sensores = filhos(filho(e, "sensores"), "sensorTermico");
        if (sensores.size() != maquina.getQuantidadeSensores()) {
            throw new PersistenciaException("A estação " + tipo + " tem " + maquina.getQuantidadeSensores()
                    + " sensores térmicos, mas o arquivo descreve " + sensores.size() + ".");
        }
        for (int i = 0; i < sensores.size(); i++) {
            maquina.restaurarCalibracaoSensor(i,
                    lerDouble(atributo(sensores.get(i), "calibracao"), "calibração do sensor " + (i + 1) + " da " + tipo));
        }
        return maquina;
    }

    // --- Leitura de elementos e conversão de valores, com mensagens claras ---

    private List<Element> filhos(Element pai, String nome) {
        List<Element> encontrados = new ArrayList<>();
        NodeList nos = pai.getChildNodes();
        for (int i = 0; i < nos.getLength(); i++) {
            Node no = nos.item(i);
            if (no.getNodeType() == Node.ELEMENT_NODE && no.getNodeName().equals(nome)) {
                encontrados.add((Element) no);
            }
        }
        return encontrados;
    }

    private Element filho(Element pai, String nome) {
        List<Element> encontrados = filhos(pai, nome);
        if (encontrados.isEmpty()) {
            throw new PersistenciaException("Elemento <" + nome + "> ausente dentro de <" + pai.getTagName() + ">.");
        }
        if (encontrados.size() > 1) {
            throw new PersistenciaException("Elemento <" + nome + "> repetido dentro de <" + pai.getTagName() + ">.");
        }
        return encontrados.get(0);
    }

    private String texto(Element pai, String nome) {
        String valor = filho(pai, nome).getTextContent().trim();
        if (valor.isEmpty()) {
            throw new PersistenciaException("Elemento <" + nome + "> vazio dentro de <" + pai.getTagName() + ">.");
        }
        return valor;
    }

    private String atributo(Element e, String nome) {
        if (!e.hasAttribute(nome)) {
            throw new PersistenciaException("Atributo '" + nome + "' ausente em <" + e.getTagName() + ">.");
        }
        return e.getAttribute(nome).trim();
    }

    private double lerDouble(String valor, String campo) {
        try {
            double numero = Double.parseDouble(valor);
            if (Double.isNaN(numero) || Double.isInfinite(numero)) {
                throw new NumberFormatException();
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new PersistenciaException("Valor numérico inválido para " + campo + ": '" + valor + "'.", e);
        }
    }

    private int lerInt(String valor, String campo) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new PersistenciaException("Valor inteiro inválido para " + campo + ": '" + valor + "'.", e);
        }
    }

    // Boolean.parseBoolean aceitaria qualquer texto como false; aqui só true/false valem
    private boolean lerBoolean(String valor, String campo) {
        if (valor.equals("true")) {
            return true;
        }
        if (valor.equals("false")) {
            return false;
        }
        throw new PersistenciaException("Valor inválido para " + campo + ": '" + valor + "' (use true ou false).");
    }

    private Cenario lerCenario(String valor) {
        try {
            return Cenario.valueOf(valor);
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Cenário desconhecido no arquivo: '" + valor + "'.", e);
        }
    }

    private StatusDemanda lerStatus(String valor) {
        try {
            return StatusDemanda.valueOf(valor);
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Status de demanda desconhecido no arquivo: '" + valor + "'.", e);
        }
    }
}
