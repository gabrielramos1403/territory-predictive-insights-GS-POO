package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

/** Régua (sensor ultrassônico) no canal/córrego, mede a altura da água em cm. */
public class SensorNivelCanal extends SensorSimulado {

    public SensorNivelCanal(int id, String nome, String regiao) throws DadosInvalidosException {
        super(id, nome, regiao);
    }

    @Override
    protected double gerarValor() {
        // o canal nunca fica seco, então começa em 60 cm
        return 60 + random.nextDouble() * 270;
    }

    @Override
    public TipoSensor retornarTipo() {
        return TipoSensor.NIVEL_CANAL;
    }
}
