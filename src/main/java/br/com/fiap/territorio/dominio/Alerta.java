package br.com.fiap.territorio.dominio;

import java.time.LocalDateTime;

public class Alerta {

    private long id;
    private int sensorId;
    private TipoSensor tipoSensor;
    private NivelAlerta nivel;
    private double valorPrevisto;
    private double limiteUltrapassado;
    private String estrategia;
    private String motivo;
    private LocalDateTime dataHora;

    public Alerta(int sensorId, TipoSensor tipoSensor, NivelAlerta nivel, double valorPrevisto,
            double limiteUltrapassado, String estrategia, String motivo, LocalDateTime dataHora) {
        this.sensorId = sensorId;
        this.tipoSensor = tipoSensor;
        this.nivel = nivel;
        this.valorPrevisto = valorPrevisto;
        this.limiteUltrapassado = limiteUltrapassado;
        this.estrategia = estrategia;
        this.motivo = motivo;
        this.dataHora = dataHora;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSensorId() {
        return sensorId;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public NivelAlerta getNivel() {
        return nivel;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public double getLimiteUltrapassado() {
        return limiteUltrapassado;
    }

    public String getEstrategia() {
        return estrategia;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
