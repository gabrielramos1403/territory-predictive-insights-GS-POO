package br.com.fiap.territorio.dominio;

import java.util.List;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

/** Previsão = média das últimas 5 leituras do sensor. */
public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    private static final int JANELA = 5;

    public PrevisaoMediaMovel() {
        super("Média móvel", JANELA);
    }

    @Override
    public ResultadoPrevisao prever(List<Medicao> historico) throws DadosInvalidosException {
        super.validarHistorico(historico);

        List<Medicao> ultimas = historico.subList(historico.size() - JANELA, historico.size());
        double media = calcularMedia(ultimas);
        TipoSensor tipo = historico.get(0).getTipoSensor();

        String significado = String.format("Nas últimas %d leituras o valor médio foi %.2f %s. "
                + "Se nada mudar, esse deve ser o patamar das próximas horas.", JANELA, media, tipo.getUnidade());
        return new ResultadoPrevisao(tipo, media, getNome(), significado);
    }
}
