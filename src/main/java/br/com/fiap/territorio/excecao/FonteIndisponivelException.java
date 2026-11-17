package br.com.fiap.territorio.excecao;

/**
 * Lançada quando a fonte de dados (sensor ou gerador) falha
 * e não consegue entregar uma leitura.
 */
public class FonteIndisponivelException extends Exception {

    private static final long serialVersionUID = 1L;

    public FonteIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
