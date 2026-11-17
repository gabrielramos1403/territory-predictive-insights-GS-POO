package br.com.fiap.territorio.servico;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import br.com.fiap.territorio.dominio.EstrategiaPrevisao;
import br.com.fiap.territorio.dominio.PrevisaoComparacao;
import br.com.fiap.territorio.dominio.PrevisaoMediaMovel;
import br.com.fiap.territorio.dominio.TipoPrevisao;
import br.com.fiap.territorio.excecao.DadosInvalidosException;

class FabricaPrevisaoTest {

    private FabricaPrevisao fabrica = new FabricaPrevisao();

    @AfterEach
    void limpar() {
        ConfiguracaoSistema.getInstancia().restaurarPadroes();
    }

    @Test
    void deveCriarCadaEstrategiaPeloNome() throws DadosInvalidosException {
        // Arrange
        String nomeMedia = "media_movel";
        String nomeComparacao = "COMPARACAO";

        // Act
        EstrategiaPrevisao media = fabrica.criar(nomeMedia);
        EstrategiaPrevisao comparacao = fabrica.criar(nomeComparacao);

        // Assert
        assertInstanceOf(PrevisaoMediaMovel.class, media);
        assertInstanceOf(PrevisaoComparacao.class, comparacao);
    }

    @Test
    void deveUsarEstrategiaDaConfiguracao() throws DadosInvalidosException {
        // Arrange
        ConfiguracaoSistema.getInstancia().setEstrategiaPadrao(TipoPrevisao.COMPARACAO);

        // Act
        EstrategiaPrevisao estrategia = fabrica.criarPadrao();

        // Assert
        assertInstanceOf(PrevisaoComparacao.class, estrategia);
    }

    @Test
    void deveLancarExcecaoParaEstrategiaDesconhecida() {
        // Arrange
        String nome = "BOLA_DE_CRISTAL";

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> fabrica.criar(nome));
    }

    @Test
    void configuracaoDeveSerSempreAMesmaInstancia() {
        // Arrange + Act
        ConfiguracaoSistema primeira = ConfiguracaoSistema.getInstancia();
        ConfiguracaoSistema segunda = ConfiguracaoSistema.getInstancia();

        // Assert - Singleton
        assertSame(primeira, segunda);
    }
}
