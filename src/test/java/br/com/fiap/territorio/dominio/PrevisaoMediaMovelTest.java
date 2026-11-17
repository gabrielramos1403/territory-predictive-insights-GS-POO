package br.com.fiap.territorio.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

class PrevisaoMediaMovelTest {

    private List<Medicao> criarHistorico(double... valores) throws DadosInvalidosException {
        List<Medicao> lista = new ArrayList<>();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 10, 8, 0);
        for (int i = 0; i < valores.length; i++) {
            lista.add(new Medicao(1, TipoSensor.CHUVA, valores[i], inicio.plusHours(i)));
        }
        return lista;
    }

    @Test
    void deveCalcularMediaDasUltimasCincoLeituras() throws DadosInvalidosException {
        // Arrange - as duas primeiras (100 e 100) não podem entrar na conta
        List<Medicao> historico = criarHistorico(100, 100, 10, 20, 30, 40, 50);
        PrevisaoMediaMovel previsao = new PrevisaoMediaMovel();

        // Act
        ResultadoPrevisao resultado = previsao.prever(historico);

        // Assert - (10+20+30+40+50) / 5 = 30
        assertEquals(30.0, resultado.getValorPrevisto(), 0.001);
        assertEquals("Média móvel", resultado.getEstrategia());
    }

    @Test
    void naoDevePreverComMenosDeCincoLeituras() throws DadosInvalidosException {
        // Arrange
        List<Medicao> historico = criarHistorico(10, 20, 30, 40);
        PrevisaoMediaMovel previsao = new PrevisaoMediaMovel();

        // Act + Assert - o sistema tem que ficar quieto
        assertThrows(DadosInvalidosException.class, () -> previsao.prever(historico));
    }

    @Test
    void naoDeveAceitarHistoricoDeSensoresDiferentes() throws DadosInvalidosException {
        // Arrange
        List<Medicao> historico = criarHistorico(10, 20, 30, 40);
        historico.add(new Medicao(2, TipoSensor.NIVEL_CANAL, 200, LocalDateTime.now()));

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> new PrevisaoMediaMovel().prever(historico));
    }
}
