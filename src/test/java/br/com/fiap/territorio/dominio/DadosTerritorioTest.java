package br.com.fiap.territorio.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.servico.ConfiguracaoSistema;

class DadosTerritorioTest {

    private DadosTerritorio territorio;

    @BeforeEach
    void preparar() throws DadosInvalidosException {
        ConfiguracaoSistema.getInstancia().restaurarPadroes();
        territorio = new DadosTerritorio("Zona Leste", "EMEF do bairro", 3000, "1234");
    }

    @Test
    void naoDeveAceitarPopulacaoNegativa() {
        // Arrange
        int populacao = -50;

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> territorio.setPopulacaoEstimada(populacao));
        assertEquals(3000, territorio.getPopulacaoEstimada());
    }

    @Test
    void deveAlterarLimiarComCodigoCorreto() throws DadosInvalidosException {
        // Arrange
        String codigo = "1234";

        // Act
        territorio.alterarLimiares(codigo, TipoSensor.CHUVA, 15, 30, 60);

        // Assert
        Limiar limiar = ConfiguracaoSistema.getInstancia().getLimiar(TipoSensor.CHUVA);
        assertEquals(15, limiar.getAtencao(), 0.001);
        assertEquals(60, limiar.getCritico(), 0.001);
    }

    @Test
    void naoDeveAlterarLimiarComCodigoErrado() throws DadosInvalidosException {
        // Arrange
        double atencaoAntes = ConfiguracaoSistema.getInstancia().getLimiar(TipoSensor.CHUVA).getAtencao();

        // Act
        assertThrows(DadosInvalidosException.class,
                () -> territorio.alterarLimiares("errado", TipoSensor.CHUVA, 15, 30, 60));

        // Assert
        assertEquals(atencaoAntes, ConfiguracaoSistema.getInstancia().getLimiar(TipoSensor.CHUVA).getAtencao(), 0.001);
    }

    @Test
    void naoDeveAceitarLimiaresForaDeOrdem() {
        // Arrange - alerta menor que atenção não faz sentido
        double atencao = 50;
        double alerta = 20;
        double critico = 80;

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> territorio.alterarLimiares("1234", TipoSensor.CHUVA, atencao, alerta, critico));
    }
}
