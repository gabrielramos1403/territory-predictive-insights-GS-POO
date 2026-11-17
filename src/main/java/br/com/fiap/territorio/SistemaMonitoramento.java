package br.com.fiap.territorio;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import br.com.fiap.territorio.dominio.Alerta;
import br.com.fiap.territorio.dominio.DadosTerritorio;
import br.com.fiap.territorio.dominio.EstrategiaPrevisao;
import br.com.fiap.territorio.dominio.Limiar;
import br.com.fiap.territorio.dominio.Medicao;
import br.com.fiap.territorio.dominio.PontoMonitoramento;
import br.com.fiap.territorio.dominio.ResultadoPrevisao;
import br.com.fiap.territorio.dominio.SensorChuva;
import br.com.fiap.territorio.dominio.SensorNivelCanal;
import br.com.fiap.territorio.dominio.SensorSimulado;
import br.com.fiap.territorio.dominio.SensorUmidadeSolo;
import br.com.fiap.territorio.dominio.TipoPrevisao;
import br.com.fiap.territorio.dominio.TipoSensor;
import br.com.fiap.territorio.excecao.DadosInvalidosException;
import br.com.fiap.territorio.excecao.FonteIndisponivelException;
import br.com.fiap.territorio.persistencia.AlertaDAO;
import br.com.fiap.territorio.persistencia.ConexaoBanco;
import br.com.fiap.territorio.persistencia.MedicaoDAO;
import br.com.fiap.territorio.servico.ConfiguracaoSistema;
import br.com.fiap.territorio.servico.ExportadorCsv;
import br.com.fiap.territorio.servico.FabricaPrevisao;
import br.com.fiap.territorio.servico.ServicoAlerta;
import br.com.fiap.territorio.servico.ServicoMonitoramento;

public class SistemaMonitoramento {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final int MAX_RODADAS = 20;

    private Scanner scanner = new Scanner(System.in);
    private MedicaoDAO medicaoDAO = new MedicaoDAO();
    private AlertaDAO alertaDAO = new AlertaDAO();
    private ServicoMonitoramento servicoMonitoramento = new ServicoMonitoramento(medicaoDAO);
    private ServicoAlerta servicoAlerta = new ServicoAlerta(alertaDAO);
    private FabricaPrevisao fabricaPrevisao = new FabricaPrevisao();
    private ExportadorCsv exportadorCsv = new ExportadorCsv();
    private ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstancia();

    private PontoMonitoramento ponto;
    private DadosTerritorio territorio;

    // guardado para a opção de exportar CSV
    private List<Medicao> ultimoHistorico = new ArrayList<>();
    private String ultimoSensorConsultado;

    public static void main(String[] args) {
        try {
            ConexaoBanco.criarTabelas();
            SistemaMonitoramento sistema = new SistemaMonitoramento();
            sistema.carregarTerritorio();
            sistema.executar();
        } catch (SQLException e) {
            System.out.println("Não foi possível abrir o banco de dados H2: " + e.getMessage());
            System.out.println("Verifique se o programa não está aberto em outra janela (o arquivo fica travado).");
        } catch (DadosInvalidosException e) {
            System.out.println("Erro nos dados iniciais do território: " + e.getMessage());
        }
    }

    private void carregarTerritorio() throws DadosInvalidosException {
        territorio = new DadosTerritorio("Zona Leste - várzea do córrego", "EMEF do bairro (ponto de apoio)",
                3200, "GS2026");

        ponto = new PontoMonitoramento(1, "Trecho baixo do córrego", "Zona Leste");
        ponto.adicionarSensor(new SensorChuva(1, "Pluviômetro P-01", "Zona Leste"));
        ponto.adicionarSensor(new SensorNivelCanal(2, "Régua do canal R-01", "Zona Leste"));
        ponto.adicionarSensor(new SensorUmidadeSolo(3, "Sonda de umidade U-01", "Zona Leste - encosta"));
    }

    private void executar() {
        System.out.println("==============================================");
        System.out.println("   TERRITORY PREDICTIVE INSIGHTS - ALAGAMENTOS");
        System.out.println("==============================================");
        System.out.println("Território: " + territorio.getRegiao());

        int opcao = -1;
        while (opcao != 0) {
            mostrarMenu();
            opcao = lerInteiro("Escolha uma opção: ");
            try {
                switch (opcao) {
                    case 1 -> verificarSensores();
                    case 2 -> rodarPrevisoes();
                    case 3 -> verHistoricoPorSensor();
                    case 4 -> verAlertasEmitidos();
                    case 5 -> gerenciarDadosTerritorio();
                    case 6 -> mostrarStatusCompleto();
                    case 7 -> registrarLeituraManual();
                    case 8 -> exportarHistoricoCsv();
                    case 0 -> System.out.println("Encerrando... os dados ficaram salvos no banco (pasta dados/).");
                    default -> System.out.println("Opção inválida, escolha um número do menu.");
                }
            } catch (DadosInvalidosException e) {
                System.out.println("[DADOS INVÁLIDOS] " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("[ERRO NO BANCO] Não foi possível acessar o banco de dados: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("[ERRO NO ARQUIVO] Não foi possível gerar o CSV: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("----------------- MENU -----------------");
        System.out.println("1 - Verificar sensores (fazer leituras)");
        System.out.println("2 - Rodar previsões");
        System.out.println("3 - Ver histórico de medições por sensor");
        System.out.println("4 - Ver alertas emitidos");
        System.out.println("5 - Gerenciar dados do território");
        System.out.println("6 - Status completo");
        System.out.println("7 - Registrar leitura manual");
        System.out.println("8 - Exportar último histórico consultado (CSV)");
        System.out.println("0 - Sair");
    }

    // ---------------------------------------------------------------- opção 1
    private void verificarSensores() throws SQLException {
        System.out.println("\n--- VERIFICAR SENSORES ---");
        for (SensorSimulado sensor : ponto.getSensores()) {
            System.out.println(sensor.verificarStatus());
        }
        int rodadas = lerInteiro("Quantas rodadas de leitura deseja fazer? (1 a " + MAX_RODADAS + "): ");
        if (rodadas < 1 || rodadas > MAX_RODADAS) {
            System.out.println("Quantidade inválida, vou fazer só 1 rodada.");
            rodadas = 1;
        }

        int gravadas = 0;
        int problemas = 0;
        for (int i = 1; i <= rodadas; i++) {
            System.out.println("Rodada " + i + ":");
            for (SensorSimulado sensor : ponto.getSensores()) {
                try {
                    Medicao medicao = servicoMonitoramento.lerSensor(sensor);
                    System.out.printf("  [OK] %-24s %8.2f %s%n", sensor.getNome(), medicao.getValor(),
                            medicao.getTipoSensor().getUnidade());
                    gravadas++;
                } catch (FonteIndisponivelException e) {
                    System.out.println("  [FALHA] " + e.getMessage());
                    problemas++;
                } catch (DadosInvalidosException e) {
                    System.out.println("  [DESCARTADA] " + sensor.getNome() + ": " + e.getMessage()
                            + " A leitura não foi gravada.");
                    problemas++;
                }
            }
        }
        System.out.println("Leituras gravadas no banco: " + gravadas + " | com problema: " + problemas);
    }

    // ---------------------------------------------------------------- opção 2
    private void rodarPrevisoes() throws DadosInvalidosException, SQLException {
        System.out.println("\n--- RODAR PREVISÕES ---");
        System.out.println("0 - Usar a estratégia padrão da configuração (" + configuracao.getEstrategiaPadrao() + ")");
        TipoPrevisao[] tipos = TipoPrevisao.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.println((i + 1) + " - " + fabricaPrevisao.criar(tipos[i]).getNome());
        }
        int escolha = lerInteiro("Estratégia: ");

        EstrategiaPrevisao estrategia;
        if (escolha == 0) {
            estrategia = fabricaPrevisao.criarPadrao();
        } else if (escolha >= 1 && escolha <= tipos.length) {
            estrategia = fabricaPrevisao.criar(tipos[escolha - 1]);
        } else {
            throw new DadosInvalidosException("Estratégia " + escolha + " não existe.");
        }

        System.out.println("Estratégia usada: " + estrategia.getNome()
                + " (mínimo de " + estrategia.getMinimoLeituras() + " leituras)");
        int alertas = 0;
        for (SensorSimulado sensor : ponto.getSensores()) {
            System.out.println("\n> " + sensor.getNome() + " (" + sensor.retornarTipo().getDescricao() + ")");
            try {
                ResultadoPrevisao resultado = servicoMonitoramento.gerarPrevisao(sensor, estrategia);
                System.out.printf("  Valor previsto: %.2f %s%n", resultado.getValorPrevisto(),
                        resultado.getTipoSensor().getUnidade());
                System.out.println("  O que significa: " + resultado.getSignificado());

                Optional<Alerta> alerta = servicoAlerta.avaliar(sensor, resultado);
                if (alerta.isPresent()) {
                    mostrarAlerta(alerta.get());
                    alertas++;
                } else {
                    System.out.println("  Sem alerta: a previsão está abaixo do limite de ATENÇÃO.");
                }
            } catch (DadosInvalidosException e) {
                System.out.println("  [SEM PREVISÃO] " + e.getMessage());
            }
        }
        System.out.println("\nAlertas emitidos nesta rodada: " + alertas);
    }

    // ---------------------------------------------------------------- opção 3
    private void verHistoricoPorSensor() throws DadosInvalidosException, SQLException {
        SensorSimulado sensor = escolherSensor();
        List<Medicao> historico = servicoMonitoramento.buscarHistoricoPorSensor(sensor.getId());

        System.out.println("\n--- HISTÓRICO: " + sensor.getNome() + " ---");
        if (historico.isEmpty()) {
            System.out.println("Nenhuma leitura gravada para esse sensor ainda.");
            return;
        }
        double soma = 0;
        for (Medicao m : historico) {
            System.out.printf("#%-5d %s  %8.2f %s%n", m.getId(), m.getDataHora().format(FORMATO_DATA),
                    m.getValor(), m.getTipoSensor().getUnidade());
            soma += m.getValor();
        }
        System.out.printf("Total: %d leituras | média geral: %.2f %s%n", historico.size(),
                soma / historico.size(), sensor.retornarTipo().getUnidade());

        ultimoHistorico = historico;
        ultimoSensorConsultado = sensor.getNome();
        System.out.println("(Use a opção 8 para exportar esse histórico em CSV)");
    }

    // ---------------------------------------------------------------- opção 4
    private void verAlertasEmitidos() throws SQLException {
        List<Alerta> alertas = servicoAlerta.listarAlertasEmitidos();
        System.out.println("\n--- ALERTAS EMITIDOS ---");
        if (alertas.isEmpty()) {
            System.out.println("Nenhum alerta foi emitido até agora.");
            return;
        }
        for (Alerta alerta : alertas) {
            mostrarAlerta(alerta);
        }
        System.out.println("Total: " + alertas.size() + " alerta(s)");
    }

    private void mostrarAlerta(Alerta alerta) {
        System.out.println("  !!! [" + alerta.getNivel().getDescricao() + "] "
                + alerta.getDataHora().format(FORMATO_DATA));
        System.out.println("      Por quê: " + alerta.getMotivo());
        System.out.println("      Sugestão para a equipe: " + alerta.getNivel().getRecomendacao());
    }

    // ---------------------------------------------------------------- opção 5
    private void gerenciarDadosTerritorio() throws DadosInvalidosException {
        System.out.println("\n--- GERENCIAR DADOS DO TERRITÓRIO ---");
        mostrarDadosTerritorio();
        System.out.println();
        System.out.println("1 - Alterar região");
        System.out.println("2 - Alterar equipamento de referência");
        System.out.println("3 - Alterar população estimada");
        System.out.println("4 - Alterar limiares de um sensor (pede código de acesso)");
        System.out.println("5 - Ligar / desligar sensor");
        System.out.println("6 - Alterar estratégia de previsão padrão");
        System.out.println("7 - Alterar código de acesso");
        System.out.println("0 - Voltar");
        int opcao = lerInteiro("Opção: ");

        switch (opcao) {
            case 1 -> {
                territorio.setRegiao(lerTexto("Nova região: "));
                System.out.println("Região alterada.");
            }
            case 2 -> {
                territorio.setEquipamentoReferencia(lerTexto("Novo equipamento de referência: "));
                System.out.println("Equipamento alterado.");
            }
            case 3 -> {
                territorio.setPopulacaoEstimada(lerInteiro("Nova população estimada: "));
                System.out.println("População alterada.");
            }
            case 4 -> alterarLimiares();
            case 5 -> ligarDesligarSensor();
            case 6 -> {
                TipoPrevisao[] tipos = TipoPrevisao.values();
                for (int i = 0; i < tipos.length; i++) {
                    System.out.println((i + 1) + " - " + tipos[i]);
                }
                int escolha = lerInteiro("Nova estratégia padrão: ");
                if (escolha < 1 || escolha > tipos.length) {
                    throw new DadosInvalidosException("Estratégia " + escolha + " não existe.");
                }
                configuracao.setEstrategiaPadrao(tipos[escolha - 1]);
                System.out.println("Estratégia padrão agora é " + configuracao.getEstrategiaPadrao());
            }
            case 7 -> {
                String atual = lerTexto("Código atual: ");
                String novo = lerTexto("Novo código (mínimo 4 caracteres): ");
                territorio.alterarCodigoAcesso(atual, novo);
                System.out.println("Código de acesso alterado.");
            }
            case 0 -> System.out.println("Voltando ao menu principal.");
            default -> System.out.println("Opção inválida.");
        }
    }

    private void alterarLimiares() throws DadosInvalidosException {
        SensorSimulado sensor = escolherSensor();
        TipoSensor tipo = sensor.retornarTipo();
        Limiar atual = configuracao.getLimiar(tipo);
        System.out.printf("Limiares atuais (%s): ATENÇÃO %.2f | ALERTA %.2f | CRÍTICO %.2f%n",
                tipo.getUnidade(), atual.getAtencao(), atual.getAlerta(), atual.getCritico());

        double atencao = lerDouble("Novo limite de ATENÇÃO: ");
        double alerta = lerDouble("Novo limite de ALERTA: ");
        double critico = lerDouble("Novo limite de CRÍTICO: ");
        String codigo = lerTexto("Código de acesso: ");

        territorio.alterarLimiares(codigo, tipo, atencao, alerta, critico);
        System.out.println("Limiares de " + tipo.getDescricao() + " alterados com sucesso.");
    }

    private void ligarDesligarSensor() throws DadosInvalidosException {
        SensorSimulado sensor = escolherSensor();
        if (sensor.estaAtivo()) {
            sensor.desativar();
            System.out.println(sensor.getNome() + " foi DESLIGADO (simulando um sensor com defeito).");
        } else {
            sensor.ativar();
            System.out.println(sensor.getNome() + " foi LIGADO novamente.");
        }
    }

    private void mostrarDadosTerritorio() throws DadosInvalidosException {
        System.out.println("Região: " + territorio.getRegiao());
        System.out.println("Equipamento de referência: " + territorio.getEquipamentoReferencia());
        System.out.println("População estimada: " + territorio.getPopulacaoEstimada() + " pessoas");
        System.out.println("Estratégia de previsão padrão: " + configuracao.getEstrategiaPadrao());
        System.out.println("Limiares:");
        for (TipoSensor tipo : TipoSensor.values()) {
            Limiar l = configuracao.getLimiar(tipo);
            System.out.printf("  %-16s ATENÇÃO %7.2f | ALERTA %7.2f | CRÍTICO %7.2f  (%s)%n",
                    tipo.getDescricao(), l.getAtencao(), l.getAlerta(), l.getCritico(), tipo.getUnidade());
        }
    }

    // ---------------------------------------------------------------- opção 6
    private void mostrarStatusCompleto() throws DadosInvalidosException, SQLException {
        System.out.println("\n========== STATUS COMPLETO ==========");
        System.out.println(ponto.verificarStatus());
        System.out.println();
        for (SensorSimulado sensor : ponto.getSensores()) {
            int leituras = medicaoDAO.contarPorSensor(sensor.getId());
            String obs = leituras < 5 ? " -> poucas leituras, previsão ainda não é confiável" : "";
            System.out.println(sensor.verificarStatus() + " | " + leituras + " leituras no banco" + obs);
        }
        System.out.println();
        mostrarDadosTerritorio();
        System.out.println();
        System.out.println("Alertas registrados no banco: " + alertaDAO.contar());
    }

    // ---------------------------------------------------------------- opção 7
    private void registrarLeituraManual() throws DadosInvalidosException, SQLException {
        System.out.println("\n--- LEITURA MANUAL ---");
        System.out.println("Use quando a equipe medir no local (ex: régua manual no canal).");
        SensorSimulado sensor = escolherSensor();
        TipoSensor tipo = sensor.retornarTipo();
        double valor = lerDouble("Valor medido em " + tipo.getUnidade() + " (" + tipo.getValorMinimo()
                + " a " + tipo.getValorMaximo() + "): ");
        Medicao medicao = servicoMonitoramento.registrarLeituraManual(sensor, valor);
        System.out.printf("Leitura de %.2f %s gravada no banco (%s).%n", medicao.getValor(), tipo.getUnidade(),
                sensor.getNome());
    }

    // ---------------------------------------------------------------- opção 8
    private void exportarHistoricoCsv() throws DadosInvalidosException, IOException {
        Path arquivo = exportadorCsv.exportar(ultimoSensorConsultado, ultimoHistorico);
        System.out.println("CSV gerado com " + ultimoHistorico.size() + " leituras de " + ultimoSensorConsultado);
        System.out.println("Arquivo: " + arquivo.toAbsolutePath());
    }

    // ---------------------------------------------------------------- utilitários
    private SensorSimulado escolherSensor() throws DadosInvalidosException {
        for (SensorSimulado sensor : ponto.getSensores()) {
            System.out.println(sensor.getId() + " - " + sensor.getNome() + " (" + sensor.retornarTipo().getDescricao() + ")");
        }
        int id = lerInteiro("Id do sensor: ");
        return ponto.buscarSensor(id);
    }

    private int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = scanner.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("'" + texto + "' não é um número inteiro. Tente de novo.");
            }
        }
    }

    private double lerDouble(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = scanner.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                System.out.println("'" + texto + "' não é um número válido. Tente de novo (ex: 12,5).");
            }
        }
    }

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine();
    }
}
