package br.com.fiap.territorio.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

class PrevisaoComparacaoTest {

    private List<Medicao> criarHistorico(double... valores) throws DadosInvalidosException {
        List<Medicao> lista = new ArrayList<>();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 10, 8, 0);
        for (int i = 0; i < valores.length; i++) {
            lista.add(new Medicao(2, TipoSensor.NIVEL_CANAL, valores[i], inicio.plusMinutes(i * 30)));
        }
        return lista;
    }

    @Test
    void deveProjetarAltaQuandoNivelEstaSubindo() throws DadosInvalidosException {
        // Arrange - anterior: média 100 | recente: média 160 | variação +60
        List<Medicao> historico = criarHistorico(90, 100, 110, 150, 160, 170);

        // Act
        ResultadoPrevisao resultado = new PrevisaoComparacao().prever(historico);

        // Assert - 160 + 60 = 220
        assertEquals(220.0, resultado.getValorPrevisto(), 0.001);
        assertTrue(resultado.getSignificado().contains("SUBINDO"));
    }

    @Test
    void naoDevePreverValorNegativoQuandoCaiMuito() throws DadosInvalidosException {
        // Arrange - anterior: 300 | recente: 100 | 100 + (-200) = -100
        List<Medicao> historico = criarHistorico(300, 300, 300, 100, 100, 100);

        // Act
        ResultadoPrevisao resultado = new PrevisaoComparacao().prever(historico);

        // Assert
        assertEquals(0.0, resultado.getValorPrevisto(), 0.001);
        assertTrue(resultado.getSignificado().contains("CAINDO"));
    }

    @Test
    void precisaDeSeisLeiturasParaComparar() throws DadosInvalidosException {
        // Arrange
        List<Medicao> historico = criarHistorico(100, 110, 120, 130, 140);

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> new PrevisaoComparacao().prever(historico));
    }

    @Test
    void estrategiasDiferentesDevemDarResultadosDiferentes() throws DadosInvalidosException {
        // Arrange - mesmo histórico para as duas
        List<Medicao> historico = criarHistorico(90, 100, 110, 150, 160, 170);
        EstrategiaPrevisao media = new PrevisaoMediaMovel();
        EstrategiaPrevisao comparacao = new PrevisaoComparacao();

        // Act
        double valorMedia = media.prever(historico).getValorPrevisto();
        double valorComparacao = comparacao.prever(historico).getValorPrevisto();

        // Assert - média das últimas 5 = 138 | comparação = 220
        assertEquals(138.0, valorMedia, 0.001);
        assertEquals(220.0, valorComparacao, 0.001);
    }
}
