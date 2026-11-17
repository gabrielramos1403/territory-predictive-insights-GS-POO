package br.com.fiap.territorio.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;

class ExportadorCsvTest {

    @TempDir
    Path pastaTemporaria;

    @Test
    void deveGerarCsvComCabecalhoEUmaLinhaPorMedicao() throws Exception {
        // Arrange
        List<Medicao> historico = new ArrayList<>();
        historico.add(new Medicao(1, TipoSensor.CHUVA, 5.5, LocalDateTime.of(2026, 11, 10, 8, 0)));
        historico.add(new Medicao(1, TipoSensor.CHUVA, 12.0, LocalDateTime.of(2026, 11, 10, 9, 0)));
        ExportadorCsv exportador = new ExportadorCsv(pastaTemporaria);

        // Act
        Path arquivo = exportador.exportar("Pluviômetro P-01", historico);
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);

        // Assert
        assertEquals(3, linhas.size());
        assertEquals("id;sensor;tipo;valor;unidade;data_hora", linhas.get(0));
        assertTrue(linhas.get(1).contains("5,50"));
        assertTrue(linhas.get(2).contains("10/11/2026 09:00:00"));
    }

    @Test
    void naoDeveExportarHistoricoVazio() {
        // Arrange
        ExportadorCsv exportador = new ExportadorCsv(pastaTemporaria);

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> exportador.exportar("Pluviômetro", new ArrayList<>()));
    }
}
