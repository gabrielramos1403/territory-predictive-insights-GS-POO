package br.com.fiap.territorio.servico;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.excecao.DadosInvalidosException;

/** Bônus: exporta o histórico que foi consultado no menu para um arquivo CSV. */
public class ExportadorCsv {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATO_ARQUIVO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private Path pasta;

    public ExportadorCsv() {
        this(Path.of("exportacoes"));
    }

    public ExportadorCsv(Path pasta) {
        this.pasta = pasta;
    }

    public Path exportar(String nomeSensor, List<Medicao> historico) throws DadosInvalidosException, IOException {
        if (historico == null || historico.isEmpty()) {
            throw new DadosInvalidosException("Não há histórico para exportar. Consulte o histórico de um sensor "
                    + "que tenha leituras primeiro.");
        }
        Files.createDirectories(pasta);
        int sensorId = historico.get(0).getSensorId();
        Path arquivo = pasta.resolve("historico_sensor_" + sensorId + "_" + LocalDateTime.now().format(FORMATO_ARQUIVO) + ".csv");

        try (PrintWriter escritor = new PrintWriter(Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8))) {
            escritor.println("id;sensor;tipo;valor;unidade;data_hora");
            for (Medicao m : historico) {
                // valor com vírgula para abrir certinho no Excel em português
                String valor = String.format("%.2f", m.getValor()).replace('.', ',');
                escritor.println(m.getId() + ";" + nomeSensor + ";" + m.getTipoSensor() + ";" + valor + ";"
                        + m.getTipoSensor().getUnidade() + ";" + m.getDataHora().format(FORMATO_DATA));
            }
        }
        return arquivo;
    }
}
