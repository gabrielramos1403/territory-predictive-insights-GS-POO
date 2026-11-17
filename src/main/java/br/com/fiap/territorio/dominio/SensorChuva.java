package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

/** Pluviômetro: chuva acumulada na última hora (mm/h). */
public class SensorChuva extends SensorSimulado {

    public SensorChuva(int id, String nome, String regiao) throws DadosInvalidosException {
        super(id, nome, regiao);
    }

    @Override
    protected double gerarValor() {
        // na maior parte do tempo chove pouco, mas às vezes vem um temporal
        double sorteio = random.nextDouble();
        if (sorteio < 0.6) {
            return random.nextDouble() * 8;
        } else if (sorteio < 0.9) {
            return 8 + random.nextDouble() * 22;
        }
        return 30 + random.nextDouble() * 50;
    }

    @Override
    public TipoSensor retornarTipo() {
        return TipoSensor.CHUVA;
    }
}
