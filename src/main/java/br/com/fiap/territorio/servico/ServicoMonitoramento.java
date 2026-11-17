package br.com.fiap.territorio.servico;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import br.com.fiap.territorio.dominio.EstrategiaPrevisao;
import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.ResultadoPrevisao;
import br.com.fiap.territorio.dominio.Sensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.excecao.FonteIndisponivelException;
import br.com.fiap.territorio.persistencia.MedicaoDAO;

public class ServicoMonitoramento {

    private MedicaoDAO medicaoDAO;

    public ServicoMonitoramento(MedicaoDAO medicaoDAO) {
        this.medicaoDAO = medicaoDAO;
    }

    /**
     * Lê o sensor, valida o valor e só grava no banco se estiver tudo certo.
     * Se a leitura vier com defeito a Medicao lança DadosInvalidosException e nada é salvo.
     */
    public Medicao lerSensor(Sensor sensor)
            throws FonteIndisponivelException, DadosInvalidosException, SQLException {
        if (sensor == null) {
            throw new DadosInvalidosException("Nenhum sensor foi informado para leitura.");
        }
        if (!sensor.verificarFuncionamento()) {
            throw new FonteIndisponivelException("O sensor " + sensor.getNome()
                    + " está desligado. Ligue ele em 'Gerenciar dados do território'.");
        }
        double valor = sensor.lerValor();
        Medicao medicao = new Medicao(sensor.getId(), sensor.retornarTipo(), valor, LocalDateTime.now());
        medicaoDAO.salvar(medicao);
        return medicao;
    }

    /** Leitura feita por uma pessoa da equipe (régua manual, pluviômetro de garrafa...). */
    public Medicao registrarLeituraManual(Sensor sensor, double valor) throws DadosInvalidosException, SQLException {
        if (sensor == null) {
            throw new DadosInvalidosException("Nenhum sensor foi informado para a leitura manual.");
        }
        Medicao medicao = new Medicao(sensor.getId(), sensor.retornarTipo(), valor, LocalDateTime.now());
        medicaoDAO.salvar(medicao);
        return medicao;
    }

    public List<Medicao> buscarHistoricoPorSensor(int sensorId) throws SQLException, DadosInvalidosException {
        return medicaoDAO.listarPorSensor(sensorId);
    }

    /** Busca o histórico do banco e passa para a estratégia escolhida. */
    public ResultadoPrevisao gerarPrevisao(Sensor sensor, EstrategiaPrevisao estrategia)
            throws SQLException, DadosInvalidosException {
        if (sensor == null || estrategia == null) {
            throw new DadosInvalidosException("Sensor e estratégia são obrigatórios para a previsão.");
        }
        List<Medicao> historico = medicaoDAO.listarPorSensor(sensor.getId());
        return estrategia.prever(historico);
    }
}
