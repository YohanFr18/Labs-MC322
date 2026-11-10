package br.unicamp.ic.mc322.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classe abstrata que representa uma estação genérica da linha de produção.
 * Concentra o estado comum (ligada, saúde, desgaste, falha) e é o Todo de uma
 * composição estrita com suas partes internas: um {@link MotorEsteira} (1:1),
 * uma {@link CamaraVaco} (1:1) e vários {@link SensorTermico} (1:N).
 *
 * <p>As partes são criadas no construtor e nunca saem da máquina: não há
 * getters para elas. Quem usa a máquina conversa só com ela, e a máquina
 * delega às partes (Lei de Deméter).</p>
 *
 * @author Victor Rimes e Yohan Rocha
 */
public abstract class Maquina implements Auditavel {
    private static final double LIMIAR_CRITICO = 30.0;
    // Acréscimo na probabilidade de falha quando algum sensor indica superaquecimento
    private static final float PENALIDADE_SUPERAQUECIMENTO = 0.15f;

    private String nome;
    private boolean ligada;
    private int capacidadeMaxima;
    private float probabilidadeFalha;
    private double custoOperacao;
    private Random randomNum;
    private double saude;
    private float multiplicadorFalhaCenario;

    // Composição estrita: partes 1:1 e 1:N
    private MotorEsteira motor;
    private CamaraVaco camara;
    private List<SensorTermico> sensores;

    /**
     * Cria a máquina com saúde 100%, desligada, e instancia ela mesma todas as
     * suas partes internas. Recebe só valores primitivos de configuração, para
     * que nenhuma parte seja criada fora da máquina nem compartilhada com outra.
     *
     * @param nome Nome identificador da máquina.
     * @param capacidadeMaxima Limite máximo de processamento.
     * @param probabilidadeFalha Risco base de a máquina falhar durante a operação.
     * @param custoOperacao Custo debitado do budget a cada unidade processada.
     * @param qtdSensores Número de sensores térmicos instalados (pelo menos 1).
     * @throws IllegalArgumentException Se {@code qtdSensores} for menor que 1.
     */
    public Maquina(String nome, int capacidadeMaxima, float probabilidadeFalha, double custoOperacao, int qtdSensores) {
        if (qtdSensores < 1) {
            throw new IllegalArgumentException("Uma máquina precisa de pelo menos 1 sensor térmico: " + qtdSensores);
        }
        this.nome = nome;
        this.ligada = false;
        this.capacidadeMaxima = capacidadeMaxima;
        this.probabilidadeFalha = probabilidadeFalha;
        this.custoOperacao = custoOperacao;
        this.randomNum = new Random();
        this.saude = 100.0;
        this.multiplicadorFalhaCenario = 1.0f;

        // A máquina cria as próprias partes: o ciclo de vida delas é o da máquina
        this.motor = new MotorEsteira();
        this.camara = new CamaraVaco();
        this.sensores = new ArrayList<>();
        for (int i = 0; i < qtdSensores; i++) {
            this.sensores.add(new SensorTermico("Sensor-" + (i + 1)));
        }
    }

    /**
     * Define o multiplicador do cenário ativo, aplicado sobre a probabilidade de falha.
     *
     * @param multiplicadorFalhaCenario Fator do cenário (acima de 1 aumenta as falhas).
     */
    public void setMultiplicadorFalhaCenario(float multiplicadorFalhaCenario) {
        this.multiplicadorFalhaCenario = multiplicadorFalhaCenario;
    }

    /**
     * Verifica se a máquina está parada, seja pela saúde zerada, seja por uma
     * parte essencial em falha: motor, câmara de vácuo ou mais da metade dos
     * sensores térmicos.
     *
     * @return {@code true} se a máquina estiver quebrada.
     */
    public boolean estaQuebrada() {
        return descreverAvaria() != null;
    }

    /**
     * Descreve por que a máquina está parada, para as mensagens de erro e a
     * auditoria. A causa vem das partes, sem expor as partes em si.
     *
     * @return O motivo da avaria, ou {@code null} se a máquina não estiver quebrada.
     */
    public String descreverAvaria() {
        if (this.saude <= 0.0) {
            return "saúde em 0%";
        }
        if (motor.isEmFalha()) {
            return "motor da esteira em falha crítica";
        }
        if (camara.isDespressurizada()) {
            return "câmara de vácuo despressurizada";
        }
        int sensoresInoperantes = contarSensoresInoperantes();
        if (sensoresInoperantes > sensores.size() / 2) {
            return sensoresInoperantes + " de " + sensores.size() + " sensores térmicos inoperantes";
        }
        return null;
    }

    private int contarSensoresInoperantes() {
        int inoperantes = 0;
        for (SensorTermico s : sensores) {
            if (s.isFalhou()) {
                inoperantes++;
            }
        }
        return inoperantes;
    }

    /**
     * Aplica o desgaste de um ciclo, influenciado pela severidade do cenário.
     * O desgaste também é repassado a todas as partes internas.
     *
     * @param multiplicadorCenario Fator multiplicativo sobre o desgaste base gerado.
     */
    public void aplicarDesgaste(double multiplicadorCenario) {
        double perda = (randomNum.nextDouble() * 3.0) * multiplicadorCenario;
        this.saude = Math.max(0.0, this.saude - perda);

        motor.aumentarDesgaste(multiplicadorCenario);
        camara.desgastarVedacoes(multiplicadorCenario);
        for (SensorTermico s : sensores) {
            s.aumentarDesgaste(multiplicadorCenario);
        }
    }

    /**
     * Pede uma leitura a todos os sensores e informa se a temperatura está
     * dentro do limite. Quem chama não precisa saber que os sensores existem.
     *
     * @return {@code true} se nenhum sensor indicar superaquecimento.
     */
    public boolean isTemperaturaEstavel() {
        boolean estavel = true;
        // Lemos todos os sensores (sem parar no primeiro) para atualizar todas as leituras
        for (SensorTermico s : sensores) {
            if (s.isSuperaquecido()) {
                estavel = false;
            }
        }
        return estavel;
    }

    /**
     * Delega à câmara de vácuo interna a tentativa de restaurar a pressão.
     */
    public void recalibrarPressaoCamara() {
        camara.repressurizar();
    }

    @Override
    public boolean precisaManutencao() {
        return this.saude < LIMIAR_CRITICO || estaQuebrada();
    }

    /**
     * Faz a manutenção completa: a saúde volta a 100% e todas as partes
     * internas são reparadas em cascata.
     */
    public void reparar() {
        this.saude = 100.0;
        motor.reparar();
        camara.reparar();
        for (SensorTermico s : sensores) {
            s.reparar();
        }
    }

    /**
     * Restaura a saúde e o estado ligado/desligado salvos anteriormente.
     * Usado ao recarregar a planta de um arquivo.
     *
     * @param saude Saúde da máquina, entre 0 e 100.
     * @param ligada {@code true} se a máquina estava ligada.
     * @throws IllegalArgumentException Se a saúde estiver fora de 0-100.
     */
    public void restaurarEstado(double saude, boolean ligada) {
        if (saude < 0.0 || saude > 100.0 || Double.isNaN(saude)) {
            throw new IllegalArgumentException("Saúde de " + nome + " fora de 0-100: " + saude);
        }
        this.saude = saude;
        this.ligada = ligada;
    }

    /**
     * Restaura o desgaste do motor interno (delegação: o motor não sai da máquina).
     *
     * @param desgaste Desgaste acumulado do motor; 100 ou mais significa falha.
     * @throws IllegalArgumentException Se o desgaste for negativo.
     */
    public void restaurarMotor(double desgaste) {
        motor.restaurarDesgaste(desgaste);
    }

    /**
     * Restaura o estado da câmara de vácuo interna.
     *
     * @param integridadeVedacoes Integridade das vedações, entre 0 e 100.
     * @param pressurizada {@code true} se a câmara mantém o vácuo.
     * @throws IllegalArgumentException Se a integridade estiver fora de 0-100 ou
     *                                  se a câmara estiver pressurizada abaixo de 20%.
     */
    public void restaurarCamara(double integridadeVedacoes, boolean pressurizada) {
        camara.restaurar(integridadeVedacoes, pressurizada);
    }

    /**
     * Restaura a calibração de um dos sensores térmicos internos.
     *
     * @param indice Posição do sensor, de 0 até {@link #getQuantidadeSensores()} - 1.
     * @param calibracao Calibração do sensor, entre 0 e 100.
     * @throws IllegalArgumentException Se o índice não existir ou a calibração
     *                                  estiver fora de 0-100.
     */
    public void restaurarCalibracaoSensor(int indice, double calibracao) {
        validarIndiceSensor(indice);
        sensores.get(indice).restaurarCalibracao(calibracao);
    }

    /**
     * Retorna o desgaste acumulado do motor interno.
     *
     * @return Desgaste do motor; 100 ou mais significa falha.
     */
    public double getDesgasteMotor() {
        return motor.getDesgasteAcumulado();
    }

    /**
     * Retorna a integridade das vedações da câmara de vácuo interna.
     *
     * @return Integridade das vedações, entre 0 e 100.
     */
    public double getIntegridadeVedacoesCamara() {
        return camara.getIntegridadeVedacoes();
    }

    /**
     * Informa se a câmara de vácuo interna mantém a pressão.
     *
     * @return {@code true} se a câmara estiver pressurizada.
     */
    public boolean isCamaraPressurizada() {
        return camara.isPressurizada();
    }

    /**
     * Retorna quantos sensores térmicos a máquina tem.
     *
     * @return Quantidade de sensores (pelo menos 1).
     */
    public int getQuantidadeSensores() {
        return sensores.size();
    }

    /**
     * Retorna a calibração de um dos sensores térmicos internos.
     *
     * @param indice Posição do sensor, de 0 até {@link #getQuantidadeSensores()} - 1.
     * @return Calibração do sensor, entre 0 e 100.
     * @throws IllegalArgumentException Se o índice não existir.
     */
    public double getCalibracaoSensor(int indice) {
        validarIndiceSensor(indice);
        return sensores.get(indice).getCalibracao();
    }

    private void validarIndiceSensor(int indice) {
        if (indice < 0 || indice >= sensores.size()) {
            throw new IllegalArgumentException(nome + " não tem sensor de índice " + indice
                    + " (tem " + sensores.size() + ").");
        }
    }

    /**
     * Sorteia se ocorreu uma falha na operação, usando a probabilidade base da máquina.
     *
     * @return {@code true} se houve falha.
     */
    protected boolean verificarFalha() {
        return verificarFalha(this.probabilidadeFalha);
    }

    /**
     * Sorteia se ocorreu uma falha a partir de uma probabilidade informada pela
     * estação (por exemplo, a de rejeição na metrologia). O desgaste, o
     * superaquecimento e o cenário aumentam essa probabilidade.
     *
     * @param probabilidadeBase Probabilidade de falha antes dos acréscimos, entre 0 e 1.
     * @return {@code true} se houve falha; sempre {@code true} se a máquina estiver quebrada.
     */
    protected boolean verificarFalha(float probabilidadeBase) {
        if (estaQuebrada()) {
            return true;
        }
        float acrescimoDesgaste = (float) ((100.0 - this.saude) / 100.0) * 0.4f;
        float acrescimoTemperatura = isTemperaturaEstavel() ? 0.0f : PENALIDADE_SUPERAQUECIMENTO;
        float probabilidadeReal = Math.min(1.0f,
                (probabilidadeBase + acrescimoDesgaste + acrescimoTemperatura) * multiplicadorFalhaCenario);
        return randomNum.nextFloat() <= probabilidadeReal;
    }

    /**
     * Gera o diagnóstico da máquina e, em cascata, o de cada parte interna,
     * uma por linha e indentadas abaixo da máquina.
     *
     * @return Relatório com várias linhas: a máquina e depois suas partes.
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        String estado;
        if (estaQuebrada()) {
            estado = "PARADA (" + descreverAvaria() + ")";
        } else if (precisaManutencao()) {
            estado = "REQUER MANUTENÇÃO";
        } else {
            estado = "OPERACIONAL";
        }

        String relatorio = String.format("[%s] %s | Saúde: %.1f%% | Ligada: %s | Estado: %s",
                getTipo(), this.nome, this.saude, this.ligada ? "sim" : "não", estado);
        relatorio += "\n    > " + motor.obterStatus();
        relatorio += "\n    > " + camara.obterStatus();
        for (SensorTermico s : sensores) {
            relatorio += "\n    > " + s.obterStatus();
        }
        return relatorio;
    }

    /**
     * Processa um único wafer/produto nesta fase específica da linha de montagem.
     *
     * @param produto Instância do produto que está passando pela máquina.
     * @return {@code true} se o processamento avançou com sucesso, {@code false} em caso de bloqueio ou erro grave.
     */
    public abstract boolean processar(Produto produto);

    /**
     * Retorna o tipo funcional da máquina para fins de registro e auditoria.
     *
     * @return String identificando o tipo (ex: "Processamento", "Inspeção").
     */
    public abstract String getTipo();

    /**
     * Liga a máquina; só máquinas ligadas processam produtos.
     */
    public void ligar() { ligada = true; }

    /**
     * Desliga a máquina, tirando-a da linha de produção.
     */
    public void desligar() { ligada = false; }

    /**
     * Retorna o nome da máquina.
     *
     * @return Nome identificador da máquina.
     */
    public String getNome() { return nome; }

    /**
     * Retorna quanto a máquina custa a cada unidade processada.
     *
     * @return Custo de operação, em reais.
     */
    public double getCustoOperacao() { return custoOperacao; }

    /**
     * Informa se a máquina está ligada.
     *
     * @return {@code true} se a máquina estiver ligada.
     */
    public boolean estaLigada() { return ligada; }

    /**
     * Retorna a probabilidade base de falha, sem desgaste nem multiplicador de cenário.
     *
     * @return Probabilidade base de falha, entre 0 e 1.
     */
    public float getProbabilidadeFalha() { return probabilidadeFalha; }

    /**
     * Retorna a capacidade máxima de processamento da máquina.
     *
     * @return Capacidade máxima de processamento.
     */
    public int getCapacidadeMaxima() { return capacidadeMaxima; }

    /**
     * Retorna a saúde atual da máquina; com 0% ela está quebrada.
     *
     * @return Saúde atual, entre 0 e 100.
     */
    public double getSaude() { return saude; }
}
