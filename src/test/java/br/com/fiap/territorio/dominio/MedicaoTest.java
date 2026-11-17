package br.com.fiap.territorio.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

class MedicaoTest {

    @Test
    void deveCriarMedicaoValida() throws DadosInvalidosException {
        // Arrange
        LocalDateTime agora = LocalDateTime.of(2026, 11, 10, 14, 0);

        // Act
        Medicao medicao = new Medicao(1, TipoSensor.CHUVA, 12.5, agora);

        // Assert
        assertEquals(12.5, medicao.getValor(), 0.001);
        assertEquals(TipoSensor.CHUVA, medicao.getTipoSensor());
        assertEquals(agora, medicao.getDataHora());
    }

    @Test
    void deveLancarExcecaoParaChuvaNegativa() {
        // Arrange
        LocalDateTime agora = LocalDateTime.now();

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> new Medicao(1, TipoSensor.CHUVA, -3, agora));

        // Assert
        assertTrue(erro.getMessage().contains("negativa"));
    }

    @Test
    void deveLancarExcecaoParaValorAcimaDaFaixa() {
        // Arrange
        double umidadeImpossivel = 130; // umidade vai só até 100%

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> new Medicao(3, TipoSensor.UMIDADE_SOLO, umidadeImpossivel, LocalDateTime.now()));
    }

    @Test
    void setterNaoDeveAlterarValorQuandoInvalido() throws DadosInvalidosException {
        // Arrange
        Medicao medicao = new Medicao(2, TipoSensor.NIVEL_CANAL, 180, LocalDateTime.now());

        // Act
        assertThrows(DadosInvalidosException.class, () -> medicao.setValor(Double.NaN));

        // Assert - continua com o valor antigo
        assertEquals(180, medicao.getValor(), 0.001);
    }
}
