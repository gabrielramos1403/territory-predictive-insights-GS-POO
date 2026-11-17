package br.com.fiap.territorio.excecao;

/**
 * Lançada quando algum dado não passa na validação.
 * Ex: medição de chuva negativa, população negativa, limiar inválido.
 */
public class DadosInvalidosException extends Exception {

    private static final long serialVersionUID = 1L;

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
