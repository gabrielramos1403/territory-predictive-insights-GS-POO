package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

/** Os três limites de um sensor. Precisa respeitar: atenção < alerta < crítico. */
public class Limiar {

    private double atencao;
    private double alerta;
    private double critico;

    public Limiar(double atencao, double alerta, double critico) throws DadosInvalidosException {
        if (atencao <= 0) {
            throw new DadosInvalidosException("Limiar inválido: o limite de ATENÇÃO precisa ser maior que zero.");
        }
        if (atencao >= alerta || alerta >= critico) {
            throw new DadosInvalidosException("Limiar inválido: precisa ser ATENÇÃO < ALERTA < CRÍTICO (recebido "
                    + atencao + " / " + alerta + " / " + critico + ").");
        }
        this.atencao = atencao;
        this.alerta = alerta;
        this.critico = critico;
    }

    public double getAtencao() {
        return atencao;
    }

    public double getAlerta() {
        return alerta;
    }

    public double getCritico() {
        return critico;
    }
}
