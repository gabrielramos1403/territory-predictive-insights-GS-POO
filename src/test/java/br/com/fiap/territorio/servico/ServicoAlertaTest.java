package br.com.fiap.territorio.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.DadosTerritorio;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.ResultadoPrevisao;
import br.com.fiap.territorio.dominio.Sensor;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.persistencia.AlertaDAO;

/**
 * Testa a lógica de alerta SEM banco: o AlertaDAO é um mock do Mockito.
 * Limiares padrão da chuva: ATENÇÃO 10 | ALERTA 25 | CRÍTICO 50 (mm/h)
 */
class ServicoAlertaTest {

    private AlertaDAO alertaDAO;
    private Sensor sensorChuva;
    private ServicoAlerta servico;

    @BeforeEach
    void preparar() {
        ConfiguracaoSistema.getInstancia().restaurarPadroes();
        alertaDAO = mock(AlertaDAO.class);
        sensorChuva = mock(Sensor.class);
        when(sensorChuva.getId()).thenReturn(1);
        when(sensorChuva.getNome()).thenReturn("Pluviômetro P-01");
        when(sensorChuva.retornarTipo()).thenReturn(TipoSensor.CHUVA);
        servico = new ServicoAlerta(alertaDAO);
    }

    @Test
    void deveEmitirAlertaCriticoESalvarNoBanco() throws DadosInvalidosException, SQLException {
        // Arrange
        ResultadoPrevisao previsao = new ResultadoPrevisao(TipoSensor.CHUVA, 62.0, "Média móvel", "teste");

        // Act
        Optional<Alerta> alerta = servico.avaliar(sensorChuva, previsao);

        // Assert
        assertTrue(alerta.isPresent());
        assertEquals(NivelAlerta.CRITICO, alerta.get().getNivel());
        assertEquals(50.0, alerta.get().getLimiteUltrapassado(), 0.001);
        assertTrue(alerta.get().getMotivo().contains("Pluviômetro P-01"));
        verify(alertaDAO, times(1)).salvar(any(Alerta.class));
    }

    @Test
    void valorExatamenteNoLimiteJaGeraAlerta() throws DadosInvalidosException, SQLException {
        // Arrange - 25 é exatamente o limite de ALERTA
        ResultadoPrevisao previsao = new ResultadoPrevisao(TipoSensor.CHUVA, 25.0, "Média móvel", "teste");

        // Act
        Optional<Alerta> alerta = servico.avaliar(sensorChuva, previsao);

        // Assert
        assertEquals(NivelAlerta.ALERTA, alerta.get().getNivel());
        verify(alertaDAO).salvar(alerta.get());
    }

    @Test
    void naoDeveEmitirNemSalvarAlertaAbaixoDaAtencao() throws DadosInvalidosException, SQLException {
        // Arrange
        ResultadoPrevisao previsao = new ResultadoPrevisao(TipoSensor.CHUVA, 9.99, "Média móvel", "teste");

        // Act
        Optional<Alerta> alerta = servico.avaliar(sensorChuva, previsao);

        // Assert
        assertTrue(alerta.isEmpty());
        verify(alertaDAO, never()).salvar(any(Alerta.class));
    }

    @Test
    void deveRespeitarLimiarAlteradoNaConfiguracao() throws Exception {
        // Arrange - subindo a ATENÇÃO para 20, uma chuva de 15 não gera mais alerta
        DadosTerritorio territorio = new DadosTerritorio("Zona Leste", "UBS", 100, "1234");
        territorio.alterarLimiares("1234", TipoSensor.CHUVA, 20, 35, 60);
        ResultadoPrevisao previsao = new ResultadoPrevisao(TipoSensor.CHUVA, 15.0, "Média móvel", "teste");

        // Act
        Optional<Alerta> alerta = servico.avaliar(sensorChuva, previsao);

        // Assert
        assertTrue(alerta.isEmpty());
        verify(alertaDAO, never()).salvar(any(Alerta.class));
    }
}
