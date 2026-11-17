package br.com.fiap.territorio.dominio;

import java.util.Random;

import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.excecao.FonteIndisponivelException;

/**
 * Parte comum dos 3 sensores. Cada filho decide como gera o valor (gerarValor).
 * Para simular o mundo real, de vez em quando o sensor perde a comunicação
 * ou manda uma leitura com defeito (fora da faixa).
 */
public abstract class SensorSimulado extends ComponenteTerritorial implements Sensor {

    private static final double CHANCE_FALHA_COMUNICACAO = 0.03;
    private static final double CHANCE_LEITURA_DEFEITUOSA = 0.03;

    protected Random random = new Random();

    public SensorSimulado(int id, String nome, String regiao) throws DadosInvalidosException {
        super(id, nome, regiao);
    }

    protected abstract double gerarValor();

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (!verificarFuncionamento()) {
            throw new FonteIndisponivelException("O sensor " + getNome() + " está desligado.");
        }
        if (random.nextDouble() < CHANCE_FALHA_COMUNICACAO) {
            throw new FonteIndisponivelException("Falha de comunicação com o sensor " + getNome()
                    + ". Tente ler novamente.");
        }
        if (random.nextDouble() < CHANCE_LEITURA_DEFEITUOSA) {
            // valor impossível de propósito, quem chama precisa validar
            return -1 * (1 + random.nextInt(20));
        }
        return gerarValor();
    }

    @Override
    public boolean verificarFuncionamento() {
        return estaAtivo();
    }

    @Override
    public String verificarStatus() {
        return String.format("[%d] %-28s %-16s %s", getId(), getNome(), retornarTipo().getDescricao(), getStatus());
    }
}
