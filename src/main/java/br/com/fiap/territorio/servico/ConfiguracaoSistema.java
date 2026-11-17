package br.com.fiap.territorio.servico;

import java.util.EnumMap;
import java.util.Map;

import br.com.fiap.territorio.dominio.Limiar;
import br.com.fiap.territorio.dominio.TipoPrevisao;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;

/**
 * Singleton com as configurações do sistema: limiares de cada tipo de sensor
 * e a estratégia de previsão padrão. Só existe uma instância, assim o alerta
 * e a tela de gerenciar dados enxergam sempre os mesmos limiares.
 */
public class ConfiguracaoSistema {

    private static ConfiguracaoSistema instancia;

    private Map<TipoSensor, Limiar> limiares = new EnumMap<>(TipoSensor.class);
    private TipoPrevisao estrategiaPadrao;

    private ConfiguracaoSistema() {
        restaurarPadroes();
    }

    public static ConfiguracaoSistema getInstancia() {
        if (instancia == null) {
            instancia = new ConfiguracaoSistema();
        }
        return instancia;
    }

    /** Volta para os valores iniciais (também usado nos testes). */
    public void restaurarPadroes() {
        try {
            limiares.put(TipoSensor.CHUVA, new Limiar(10, 25, 50));
            limiares.put(TipoSensor.NIVEL_CANAL, new Limiar(150, 220, 300));
            limiares.put(TipoSensor.UMIDADE_SOLO, new Limiar(70, 85, 95));
        } catch (DadosInvalidosException e) {
            // só acontece se alguém digitar os valores padrão errado aqui no código
            throw new IllegalStateException("Limiares padrão estão errados: " + e.getMessage(), e);
        }
        estrategiaPadrao = TipoPrevisao.MEDIA_MOVEL;
    }

    public Limiar getLimiar(TipoSensor tipo) throws DadosInvalidosException {
        Limiar limiar = limiares.get(tipo);
        if (limiar == null) {
            throw new DadosInvalidosException("Não há limiar configurado para o sensor " + tipo + ".");
        }
        return limiar;
    }

    // chamado pelo DadosTerritorio depois de conferir o código de acesso
    public void definirLimiar(TipoSensor tipo, Limiar limiar) throws DadosInvalidosException {
        if (tipo == null || limiar == null) {
            throw new DadosInvalidosException("Tipo de sensor e limiar são obrigatórios.");
        }
        limiares.put(tipo, limiar);
    }

    public TipoPrevisao getEstrategiaPadrao() {
        return estrategiaPadrao;
    }

    public void setEstrategiaPadrao(TipoPrevisao estrategiaPadrao) throws DadosInvalidosException {
        if (estrategiaPadrao == null) {
            throw new DadosInvalidosException("Escolha uma estratégia de previsão válida.");
        }
        this.estrategiaPadrao = estrategiaPadrao;
    }
}
