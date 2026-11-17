package br.com.fiap.territorio.dominio;

import java.util.List;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

/**
 * Compara a média das 3 leituras mais recentes com a média das 3 anteriores.
 * A previsão é a média recente somada com a variação (se está subindo, continua subindo).
 */
public class PrevisaoComparacao extends EstrategiaPrevisao {

    private static final int TAMANHO_PERIODO = 3;

    public PrevisaoComparacao() {
        super("Comparação com período anterior", TAMANHO_PERIODO * 2);
    }

    @Override
    public ResultadoPrevisao prever(List<Medicao> historico) throws DadosInvalidosException {
        super.validarHistorico(historico);

        int fim = historico.size();
        List<Medicao> anterior = historico.subList(fim - TAMANHO_PERIODO * 2, fim - TAMANHO_PERIODO);
        List<Medicao> recente = historico.subList(fim - TAMANHO_PERIODO, fim);

        double mediaAnterior = calcularMedia(anterior);
        double mediaRecente = calcularMedia(recente);
        double variacao = mediaRecente - mediaAnterior;

        double previsto = mediaRecente + variacao;
        if (previsto < 0) {
            previsto = 0; // chuva, nível e umidade não ficam negativos
        }

        TipoSensor tipo = historico.get(0).getTipoSensor();
        String tendencia;
        if (variacao > 0) {
            tendencia = "SUBINDO";
        } else if (variacao < 0) {
            tendencia = "CAINDO";
        } else {
            tendencia = "ESTÁVEL";
        }

        String significado = String.format("Tendência %s: o período anterior teve média %.2f e o atual %.2f %s "
                + "(variação de %+.2f). Mantendo esse ritmo, o próximo período deve ficar perto de %.2f %s.",
                tendencia, mediaAnterior, mediaRecente, tipo.getUnidade(), variacao, previsto, tipo.getUnidade());
        return new ResultadoPrevisao(tipo, previsto, getNome(), significado);
    }
}
