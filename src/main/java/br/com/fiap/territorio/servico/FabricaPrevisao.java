package br.com.fiap.territorio.servico;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import br.com.fiap.territorio.dominio.EstrategiaPrevisao;
import br.com.fiap.territorio.dominio.PrevisaoComparacao;
import br.com.fiap.territorio.dominio.PrevisaoMediaMovel;
import br.com.fiap.territorio.dominio.TipoPrevisao;
import br.com.fiap.territorio.excecao.DadosInvalidosException;

/**
 * Factory das estratégias de previsão. Cada tipo fica registrado no mapa,
 * então para criar uma estratégia não precisa de if/switch espalhado pelo código.
 * Para adicionar uma estratégia nova é só colocar mais uma linha no construtor.
 */
public class FabricaPrevisao {

    private final Map<TipoPrevisao, Supplier<EstrategiaPrevisao>> estrategias = new EnumMap<>(TipoPrevisao.class);

    public FabricaPrevisao() {
        estrategias.put(TipoPrevisao.MEDIA_MOVEL, PrevisaoMediaMovel::new);
        estrategias.put(TipoPrevisao.COMPARACAO, PrevisaoComparacao::new);
    }

    public EstrategiaPrevisao criar(TipoPrevisao tipo) throws DadosInvalidosException {
        Supplier<EstrategiaPrevisao> criador = estrategias.get(tipo);
        if (criador == null) {
            throw new DadosInvalidosException("Estratégia de previsão não cadastrada: " + tipo);
        }
        return criador.get();
    }

    /** Cria a partir do nome (ex: vindo da configuração ou digitado pelo usuário). */
    public EstrategiaPrevisao criar(String nome) throws DadosInvalidosException {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("Informe o nome da estratégia (MEDIA_MOVEL ou COMPARACAO).");
        }
        try {
            return criar(TipoPrevisao.valueOf(nome.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException("Estratégia desconhecida: '" + nome
                    + "'. Use MEDIA_MOVEL ou COMPARACAO.");
        }
    }

    /** Usa a estratégia que está definida no ConfiguracaoSistema. */
    public EstrategiaPrevisao criarPadrao() throws DadosInvalidosException {
        return criar(ConfiguracaoSistema.getInstancia().getEstrategiaPadrao());
    }
}
