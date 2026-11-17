package br.com.fiap.territorio.dominio;

import java.util.List;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

public abstract class EstrategiaPrevisao {

    private String nome;
    private int minimoLeituras;

    public EstrategiaPrevisao(String nome, int minimoLeituras) {
        this.nome = nome;
        this.minimoLeituras = minimoLeituras;
    }

    /**
     * Recebe o histórico de UM sensor em ordem cronológica (mais antiga primeiro)
     * e devolve o valor previsto para as próximas horas.
     */
    public abstract ResultadoPrevisao prever(List<Medicao> historico) throws DadosInvalidosException;

    // validação comum, os filhos chamam com super.validarHistorico(...)
    protected void validarHistorico(List<Medicao> historico) throws DadosInvalidosException {
        if (historico == null || historico.isEmpty()) {
            throw new DadosInvalidosException("Não há leituras no banco para esse sensor. "
                    + "Faça leituras antes de rodar a previsão.");
        }
        if (historico.size() < minimoLeituras) {
            throw new DadosInvalidosException("Histórico insuficiente para " + nome + ": "
                    + historico.size() + " leitura(s), o mínimo é " + minimoLeituras
                    + ". O sistema não vai arriscar uma previsão.");
        }
        int sensorId = historico.get(0).getSensorId();
        for (Medicao medicao : historico) {
            if (medicao.getSensorId() != sensorId) {
                throw new DadosInvalidosException("O histórico mistura leituras de sensores diferentes.");
            }
        }
    }

    protected double calcularMedia(List<Medicao> medicoes) {
        double soma = 0;
        for (Medicao medicao : medicoes) {
            soma += medicao.getValor();
        }
        return soma / medicoes.size();
    }

    public String getNome() {
        return nome;
    }

    public int getMinimoLeituras() {
        return minimoLeituras;
    }
}
