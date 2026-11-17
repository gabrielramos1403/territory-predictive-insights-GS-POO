package br.com.fiap.territorio.dominio;

public enum NivelAlerta {

    ATENCAO("ATENÇÃO", "Acompanhar as próximas leituras e avisar a equipe de plantão."),
    ALERTA("ALERTA", "Enviar alguém para vistoriar o ponto e preparar o abrigo de apoio."),
    CRITICO("CRÍTICO", "Acionar a Defesa Civil e orientar os moradores das áreas baixas.");

    private final String descricao;
    private final String recomendacao;

    NivelAlerta(String descricao, String recomendacao) {
        this.descricao = descricao;
        this.recomendacao = recomendacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getRecomendacao() {
        return recomendacao;
    }
}
