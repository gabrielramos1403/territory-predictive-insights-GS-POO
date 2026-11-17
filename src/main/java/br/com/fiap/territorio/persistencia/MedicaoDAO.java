package br.com.fiap.territorio.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;

public class MedicaoDAO {

    public void salvar(Medicao medicao) throws SQLException {
        String sql = "INSERT INTO medicao (sensor_id, tipo_sensor, valor, data_hora) VALUES (?, ?, ?, ?)";
        try (Connection conexao = ConexaoBanco.getConexao();
                PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, medicao.getSensorId());
            ps.setString(2, medicao.getTipoSensor().name());
            ps.setDouble(3, medicao.getValor());
            ps.setTimestamp(4, Timestamp.valueOf(medicao.getDataHora()));
            ps.executeUpdate();
        }
    }

    /** Histórico do sensor em ordem cronológica (mais antiga primeiro). */
    public List<Medicao> listarPorSensor(int sensorId) throws SQLException, DadosInvalidosException {
        String sql = "SELECT id, sensor_id, tipo_sensor, valor, data_hora FROM medicao "
                + "WHERE sensor_id = ? ORDER BY data_hora, id";
        List<Medicao> lista = new ArrayList<>();
        try (Connection conexao = ConexaoBanco.getConexao();
                PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, sensorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Medicao(
                            rs.getLong("id"),
                            rs.getInt("sensor_id"),
                            TipoSensor.valueOf(rs.getString("tipo_sensor")),
                            rs.getDouble("valor"),
                            rs.getTimestamp("data_hora").toLocalDateTime()));
                }
            }
        }
        return lista;
    }

    public int contarPorSensor(int sensorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM medicao WHERE sensor_id = ?";
        try (Connection conexao = ConexaoBanco.getConexao();
                PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, sensorId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
