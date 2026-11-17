package br.com.fiap.territorio.servico;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.Limiar;
import br.com.fiap.territorio.dominio.NivelAlerta;
import br.com.fiap.territorio.dominio.ResultadoPrevisao;
import br.com.fiap.territorio.dominio.Sensor;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.persistencia.AlertaDAO;

public class ServicoAlerta {

    private AlertaDAO alertaDAO;

    public ServicoAlerta(AlertaDAO alertaDAO) {
        this.alertaDAO = alertaDAO;
    }

    /**
     * Compara a previsão com os limiares do ConfiguracaoSistema.
     * Se passou de algum limite cria o alerta, grava no banco e devolve.
     * Se não passou, devolve vazio (não tem motivo para alarmar ninguém).
     */
    public Optional<Alerta> avaliar(Sensor sensor, ResultadoPrevisao previsao)
            throws DadosInvalidosException, SQLException {
        if (sensor == null || previsao == null) {
            throw new DadosInvalidosException("Sensor e previsão são obrigatórios para avaliar alertas.");
        }
        TipoSensor tipo = sensor.retornarTipo();
        Limiar limiar = ConfiguracaoSistema.getInstancia().getLimiar(tipo);
        double valor = previsao.getValorPrevisto();

        NivelAlerta nivel;
        double limite;
        if (valor >= limiar.getCritico()) {
            nivel = NivelAlerta.CRITICO;
            limite = limiar.getCritico();
        } else if (valor >= limiar.getAlerta()) {
            nivel = NivelAlerta.ALERTA;
            limite = limiar.getAlerta();
        } else if (valor >= limiar.getAtencao()) {
            nivel = NivelAlerta.ATENCAO;
            limite = limiar.getAtencao();
        } else {
            return Optional.empty();
        }

        String motivo = String.format("Sensor %s (%s): previsão de %.2f %s pela %s atingiu o limite de %s (%.2f %s).",
                sensor.getNome(), tipo.getDescricao(), valor, tipo.getUnidade(), previsao.getEstrategia(),
                nivel.getDescricao(), limite, tipo.getUnidade());

        Alerta alerta = new Alerta(sensor.getId(), tipo, nivel, valor, limite, previsao.getEstrategia(),
                motivo, LocalDateTime.now());
        alertaDAO.salvar(alerta);
        return Optional.of(alerta);
    }

    public List<Alerta> listarAlertasEmitidos() throws SQLException {
        return alertaDAO.listarTodos();
    }
}
