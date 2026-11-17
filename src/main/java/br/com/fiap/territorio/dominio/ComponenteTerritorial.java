package br.com.fiap.territorio.dominio;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

/**
 * Tudo que faz parte do território monitorado (sensores, pontos de monitoramento...).
 */
public abstract class ComponenteTerritorial {

    private int id;
    private String nome;
    private String regiao;
    private StatusComponente status;

    public ComponenteTerritorial(int id, String nome, String regiao) throws DadosInvalidosException {
        if (id <= 0) {
            throw new DadosInvalidosException("O id do componente deve ser maior que zero.");
        }
        setNome(nome);
        setRegiao(regiao);
        this.id = id;
        this.status = StatusComponente.ATIVO;
    }

    // cada componente sabe dizer como ele está
    public abstract String verificarStatus();

    public void ativar() {
        status = StatusComponente.ATIVO;
    }

    public void desativar() {
        status = StatusComponente.INATIVO;
    }

    public boolean estaAtivo() {
        return status == StatusComponente.ATIVO;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) throws DadosInvalidosException {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("O nome do componente não pode ficar vazio.");
        }
        this.nome = nome.trim();
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) throws DadosInvalidosException {
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("A região do componente não pode ficar vazia.");
        }
        this.regiao = regiao.trim();
    }

    public StatusComponente getStatus() {
        return status;
    }
}
