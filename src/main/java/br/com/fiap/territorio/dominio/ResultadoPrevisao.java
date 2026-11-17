package br.com.fiap.territorio.dominio;

public class ResultadoPrevisao {

    private TipoSensor tipoSensor;
    private double valorPrevisto;
    private String estrategia;
    private String significado;

    public ResultadoPrevisao(TipoSensor tipoSensor, double valorPrevisto, String estrategia, String significado) {
        this.tipoSensor = tipoSensor;
        this.valorPrevisto = valorPrevisto;
        this.estrategia = estrategia;
        this.significado = significado;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public String getEstrategia() {
        return estrategia;
    }

    public String getSignificado() {
        return significado;
    }
}
