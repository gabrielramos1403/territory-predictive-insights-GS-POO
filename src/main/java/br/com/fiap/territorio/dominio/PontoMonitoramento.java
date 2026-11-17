package br.com.fiap.territorio.dominio;

import java.util.ArrayList;
import java.util.List;

import br.com.fiap.territorio.excecao.DadosInvalidosException;

/**
 * Ponto vulnerável do bairro (ex: trecho do córrego que sempre alaga)
 * onde ficam instalados os sensores.
 */
public class PontoMonitoramento extends ComponenteTerritorial {

    private List<SensorSimulado> sensores = new ArrayList<>();

    public PontoMonitoramento(int id, String nome, String regiao) throws DadosInvalidosException {
        super(id, nome, regiao);
    }

    public void adicionarSensor(SensorSimulado sensor) throws DadosInvalidosException {
        if (sensor == null) {
            throw new DadosInvalidosException("Não é possível adicionar um sensor nulo ao ponto.");
        }
        sensores.add(sensor);
    }

    public SensorSimulado buscarSensor(int id) throws DadosInvalidosException {
        for (SensorSimulado sensor : sensores) {
            if (sensor.getId() == id) {
                return sensor;
            }
        }
        throw new DadosInvalidosException("Não existe sensor com o id " + id + ".");
    }

    public int contarSensoresAtivos() {
        int ativos = 0;
        for (SensorSimulado sensor : sensores) {
            if (sensor.verificarFuncionamento()) {
                ativos++;
            }
        }
        return ativos;
    }

    @Override
    public String verificarStatus() {
        int ativos = contarSensoresAtivos();
        String situacao;
        if (ativos == sensores.size()) {
            situacao = "todos os sensores funcionando";
        } else if (ativos == 0) {
            situacao = "SEM NENHUM SENSOR FUNCIONANDO";
        } else {
            situacao = "funcionando parcialmente";
        }
        return getNome() + " (" + getRegiao() + "): " + ativos + "/" + sensores.size()
                + " sensores ativos - " + situacao;
    }

    public List<SensorSimulado> getSensores() {
        return sensores;
    }
}
