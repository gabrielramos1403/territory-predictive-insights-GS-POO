package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

/** Sensor de umidade do solo em encosta. Solo encharcado não absorve mais a chuva. */
public class SensorUmidadeSolo extends SensorSimulado {

    public SensorUmidadeSolo(int id, String nome, String regiao) throws DadosInvalidosException {
        super(id, nome, regiao);
    }

    @Override
    protected double gerarValor() {
        return 35 + random.nextDouble() * 65;
    }

    @Override
    public TipoSensor retornarTipo() {
        return TipoSensor.UMIDADE_SOLO;
    }
}
