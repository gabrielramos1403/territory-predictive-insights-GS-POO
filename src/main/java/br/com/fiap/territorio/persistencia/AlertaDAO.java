package br.com.fiap.territorio.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.TipoSensor;

public class AlertaDAO {

    public void salvar(Alerta alerta) throws SQLException {
        String sql = "INSERT INTO alerta (sensor_id, tipo_sensor, nivel, valor_previsto, limite_ultrapassado, "
                + "estrategia, motivo, data_hora) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexao = ConexaoBanco.getConexao();
                PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, alerta.getSensorId());
            ps.setString(2, alerta.getTipoSensor().name());
            ps.setString(3, alerta.getNivel().name());
            ps.setDouble(4, alerta.getValorPrevisto());
            ps.setDouble(5, alerta.getLimiteUltrapassado());
            ps.setString(6, alerta.getEstrategia());
            ps.setString(7, alerta.getMotivo());
            ps.setTimestamp(8, Timestamp.valueOf(alerta.getDataHora()));
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    alerta.setId(chaves.getLong(1));
                }
            }
        }
    }

    /** Todos os alertas, do mais recente para o mais antigo. */
    public List<Alerta> listarTodos() throws SQLException {
        String sql = "SELECT * FROM alerta ORDER BY data_hora DESC, id DESC";
        List<Alerta> lista = new ArrayList<>();
        try (Connection conexao = ConexaoBanco.getConexao();
                PreparedStatement ps = conexao.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Alerta alerta = new Alerta(
                        rs.getInt("sensor_id"),
                        TipoSensor.valueOf(rs.getString("tipo_sensor")),
                        NivelAlerta.valueOf(rs.getString("nivel")),
                        rs.getDouble("valor_previsto"),
                        rs.getDouble("limite_ultrapassado"),
                        rs.getString("estrategia"),
                        rs.getString("motivo"),
                        rs.getTimestamp("data_hora").toLocalDateTime());
                alerta.setId(rs.getLong("id"));
                lista.add(alerta);
            }
        }
        return lista;
    }

    public int contar() throws SQLException {
        try (Connection conexao = ConexaoBanco.getConexao();
                PreparedStatement ps = conexao.prepareStatement("SELECT COUNT(*) FROM alerta");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
