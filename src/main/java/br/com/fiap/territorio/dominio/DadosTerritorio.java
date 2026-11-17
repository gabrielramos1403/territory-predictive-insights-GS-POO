package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.servico.ConfiguracaoSistema;

/**
 * Dados gerais do território. Não guardamos nenhum dado pessoal,
 * só região, equipamento público de referência e população estimada.
 * Para mexer nos limiares é preciso informar o código de acesso.
 */
public class DadosTerritorio {

    private String regiao;
    private String equipamentoReferencia;
    private int populacaoEstimada;
    private String codigoAcesso;

    public DadosTerritorio(String regiao, String equipamentoReferencia, int populacaoEstimada, String codigoAcesso)
            throws DadosInvalidosException {
        setRegiao(regiao);
        setEquipamentoReferencia(equipamentoReferencia);
        setPopulacaoEstimada(populacaoEstimada);
        validarNovoCodigo(codigoAcesso);
        this.codigoAcesso = codigoAcesso;
    }

    public void alterarLimiares(String codigo, TipoSensor tipo, double atencao, double alerta, double critico)
            throws DadosInvalidosException {
        conferirCodigo(codigo);
        if (tipo == null) {
            throw new DadosInvalidosException("Escolha o tipo de sensor para alterar o limiar.");
        }
        if (critico > tipo.getValorMaximo()) {
            throw new DadosInvalidosException("Limiar inválido: o CRÍTICO não pode passar do máximo do sensor ("
                    + tipo.getValorMaximo() + " " + tipo.getUnidade() + ").");
        }
        Limiar novo = new Limiar(atencao, alerta, critico);
        ConfiguracaoSistema.getInstancia().definirLimiar(tipo, novo);
    }

    public void alterarCodigoAcesso(String codigoAtual, String novoCodigo) throws DadosInvalidosException {
        conferirCodigo(codigoAtual);
        validarNovoCodigo(novoCodigo);
        this.codigoAcesso = novoCodigo;
    }

    private void conferirCodigo(String codigo) throws DadosInvalidosException {
        if (codigo == null || !codigo.equals(codigoAcesso)) {
            throw new DadosInvalidosException("Código de acesso incorreto. Os limiares não foram alterados.");
        }
    }

    private void validarNovoCodigo(String codigo) throws DadosInvalidosException {
        if (codigo == null || codigo.trim().length() < 4) {
            throw new DadosInvalidosException("O código de acesso precisa ter pelo menos 4 caracteres.");
        }
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) throws DadosInvalidosException {
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("A região não pode ficar vazia.");
        }
        this.regiao = regiao.trim();
    }

    public String getEquipamentoReferencia() {
        return equipamentoReferencia;
    }

    public void setEquipamentoReferencia(String equipamentoReferencia) throws DadosInvalidosException {
        if (equipamentoReferencia == null || equipamentoReferencia.isBlank()) {
            throw new DadosInvalidosException("Informe o equipamento público de referência (ex: escola, UBS).");
        }
        this.equipamentoReferencia = equipamentoReferencia.trim();
    }

    public int getPopulacaoEstimada() {
        return populacaoEstimada;
    }

    public void setPopulacaoEstimada(int populacaoEstimada) throws DadosInvalidosException {
        if (populacaoEstimada < 0) {
            throw new DadosInvalidosException("População estimada não pode ser negativa (" + populacaoEstimada + ").");
        }
        this.populacaoEstimada = populacaoEstimada;
    }
}
