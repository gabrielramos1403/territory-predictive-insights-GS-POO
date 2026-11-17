package br.com.fiap.territorio.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.PrevisaoMediaMovel;
import br.com.fiap.territorio.dominio.ResultadoPrevisao;
import br.com.fiap.territorio.dominio.Sensor;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.excecao.FonteIndisponivelException;
import br.com.fiap.territorio.persistencia.MedicaoDAO;

class ServicoMonitoramentoTest {

    private MedicaoDAO medicaoDAO;
    private Sensor sensor;
    private ServicoMonitoramento servico;

    @BeforeEach
    void preparar() {
        medicaoDAO = mock(MedicaoDAO.class);
        sensor = mock(Sensor.class);
        when(sensor.getId()).thenReturn(2);
        when(sensor.getNome()).thenReturn("Régua do canal R-01");
        when(sensor.retornarTipo()).thenReturn(TipoSensor.NIVEL_CANAL);
        servico = new ServicoMonitoramento(medicaoDAO);
    }

    @Test
    void deveGravarLeituraValida() throws Exception {
        // Arrange
        when(sensor.verificarFuncionamento()).thenReturn(true);
        when(sensor.lerValor()).thenReturn(180.0);

        // Act
        Medicao medicao = servico.lerSensor(sensor);

        // Assert
        assertEquals(180.0, medicao.getValor(), 0.001);
        verify(medicaoDAO).salvar(any(Medicao.class));
    }

    @Test
    void naoDeveGravarLeituraComDefeito() throws Exception {
        // Arrange - sensor devolveu valor negativo
        when(sensor.verificarFuncionamento()).thenReturn(true);
        when(sensor.lerValor()).thenReturn(-7.0);

        // Act
        assertThrows(DadosInvalidosException.class, () -> servico.lerSensor(sensor));

        // Assert
        verify(medicaoDAO, never()).salvar(any(Medicao.class));
    }

    @Test
    void sensorDesligadoDeveLancarFonteIndisponivel() throws SQLException {
        // Arrange
        when(sensor.verificarFuncionamento()).thenReturn(false);

        // Act
        assertThrows(FonteIndisponivelException.class, () -> servico.lerSensor(sensor));

        // Assert
        verify(medicaoDAO, never()).salvar(any(Medicao.class));
    }

    @Test
    void falhaDeComunicacaoDoSensorNaoGravaNada() throws Exception {
        // Arrange
        when(sensor.verificarFuncionamento()).thenReturn(true);
        when(sensor.lerValor()).thenThrow(new FonteIndisponivelException("sem sinal"));

        // Act
        assertThrows(FonteIndisponivelException.class, () -> servico.lerSensor(sensor));

        // Assert
        verify(medicaoDAO, never()).salvar(any(Medicao.class));
    }

    @Test
    void previsaoDeveUsarHistoricoVindoDoBanco() throws Exception {
        // Arrange - o "banco" (mock) devolve 5 leituras do canal
        List<Medicao> historico = new ArrayList<>();
        double[] valores = { 100, 120, 140, 160, 180 };
        for (int i = 0; i < valores.length; i++) {
            historico.add(new Medicao(2, TipoSensor.NIVEL_CANAL, valores[i], LocalDateTime.now().plusMinutes(i)));
        }
        when(medicaoDAO.listarPorSensor(2)).thenReturn(historico);

        // Act
        ResultadoPrevisao resultado = servico.gerarPrevisao(sensor, new PrevisaoMediaMovel());

        // Assert - (100+120+140+160+180)/5 = 140
        assertEquals(140.0, resultado.getValorPrevisto(), 0.001);
        verify(medicaoDAO).listarPorSensor(2);
    }
}
