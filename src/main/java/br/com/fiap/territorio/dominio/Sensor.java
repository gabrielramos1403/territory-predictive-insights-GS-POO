package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.FonteIndisponivelException;

public interface Sensor {

    double lerValor() throws FonteIndisponivelException;

    boolean verificarFuncionamento();

    TipoSensor retornarTipo();

    int getId();

    String getNome();
}
