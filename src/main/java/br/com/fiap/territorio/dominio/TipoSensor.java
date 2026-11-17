package br.com.fiap.territorio.dominio;

/**
 * Tipos de sensor do recorte de clima local (chuva e alagamento).
 * A faixa mínima/máxima é o que consideramos uma leitura possível.
 */
public enum TipoSensor {

    CHUVA("Chuva", "mm/h", 0, 150),
    NIVEL_CANAL("Nível do canal", "cm", 0, 400),
    UMIDADE_SOLO("Umidade do solo", "%", 0, 100);

    private final String descricao;
    private final String unidade;
    private final double valorMinimo;
    private final double valorMaximo;

    TipoSensor(String descricao, String unidade, double valorMinimo, double valorMaximo) {
        this.descricao = descricao;
        this.unidade = unidade;
        this.valorMinimo = valorMinimo;
        this.valorMaximo = valorMaximo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getValorMinimo() {
        return valorMinimo;
    }

    public double getValorMaximo() {
        return valorMaximo;
    }
}
