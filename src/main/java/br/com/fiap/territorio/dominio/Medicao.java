package br.com.fiap.territorio.dominio;

import java.time.LocalDateTime;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

public class Medicao {

    private long id;
    private int sensorId;
    private TipoSensor tipoSensor;
    private double valor;
    private LocalDateTime dataHora;

    public Medicao(int sensorId, TipoSensor tipoSensor, double valor, LocalDateTime dataHora)
            throws DadosInvalidosException {
        if (sensorId <= 0) {
            throw new DadosInvalidosException("Medição sem sensor válido (id " + sensorId + ").");
        }
        if (tipoSensor == null) {
            throw new DadosInvalidosException("Medição precisa ter o tipo do sensor.");
        }
        if (dataHora == null) {
            throw new DadosInvalidosException("Medição precisa ter data e hora.");
        }
        this.sensorId = sensorId;
        this.tipoSensor = tipoSensor;
        this.dataHora = dataHora;
        setValor(valor);
    }

    // construtor usado pelo DAO quando a medição já veio do banco
    public Medicao(long id, int sensorId, TipoSensor tipoSensor, double valor, LocalDateTime dataHora)
            throws DadosInvalidosException {
        this(sensorId, tipoSensor, valor, dataHora);
        this.id = id;
    }

    public void setValor(double valor) throws DadosInvalidosException {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw new DadosInvalidosException("Medição de " + tipoSensor.getDescricao().toLowerCase()
                    + " não é um número válido.");
        }
        if (valor < tipoSensor.getValorMinimo()) {
            throw new DadosInvalidosException("Medição de " + tipoSensor.getDescricao().toLowerCase()
                    + " negativa (" + String.format("%.2f", valor) + " " + tipoSensor.getUnidade() + ").");
        }
        if (valor > tipoSensor.getValorMaximo()) {
            throw new DadosInvalidosException("Medição de " + tipoSensor.getDescricao().toLowerCase()
                    + " fora da faixa: " + String.format("%.2f", valor) + " " + tipoSensor.getUnidade()
                    + " (máximo " + String.format("%.0f", tipoSensor.getValorMaximo()) + ").");
        }
        this.valor = valor;
    }

    public long getId() {
        return id;
    }

    public int getSensorId() {
        return sensorId;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
