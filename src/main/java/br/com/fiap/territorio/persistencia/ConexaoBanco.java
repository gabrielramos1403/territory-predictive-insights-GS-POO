package br.com.fiap.territorio.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Conexão com o H2. O banco fica salvo em arquivo (pasta dados/ na raiz do projeto),
 * por isso o histórico continua lá depois de fechar o programa.
 */
public class ConexaoBanco {

    private static final String URL = "jdbc:h2:./dados/territorio";
    private static final String USUARIO = "sa";
    private static final String SENHA = "";

    private static final String CRIAR_TABELA_MEDICAO =
            "CREATE TABLE IF NOT EXISTS medicao ("
            + " id BIGINT AUTO_INCREMENT PRIMARY KEY,"
            + " sensor_id INT NOT NULL,"
            + " tipo_sensor VARCHAR(20) NOT NULL,"
            + " valor DOUBLE NOT NULL,"
            + " data_hora TIMESTAMP NOT NULL)";

    private static final String CRIAR_TABELA_ALERTA =
            "CREATE TABLE IF NOT EXISTS alerta ("
            + " id BIGINT AUTO_INCREMENT PRIMARY KEY,"
            + " sensor_id INT NOT NULL,"
            + " tipo_sensor VARCHAR(20) NOT NULL,"
            + " nivel VARCHAR(10) NOT NULL,"
            + " valor_previsto DOUBLE NOT NULL,"
            + " limite_ultrapassado DOUBLE NOT NULL,"
            + " estrategia VARCHAR(60) NOT NULL,"
            + " motivo VARCHAR(500) NOT NULL,"
            + " data_hora TIMESTAMP NOT NULL)";

    private ConexaoBanco() {
    }

    public static Connection getConexao() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    public static void criarTabelas() throws SQLException {
        try (Connection conexao = getConexao();
                PreparedStatement psMedicao = conexao.prepareStatement(CRIAR_TABELA_MEDICAO);
                PreparedStatement psAlerta = conexao.prepareStatement(CRIAR_TABELA_ALERTA)) {
            psMedicao.execute();
            psAlerta.execute();
        }
    }
}
