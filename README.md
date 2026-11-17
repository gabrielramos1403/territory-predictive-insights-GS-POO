# Territory Predictive Insights - Monitoramento de Alagamentos

Global Solution 2º semestre/2026 - Programação Orientada a Objetos (2CCPH)
Tema: CitizenTech | Recorte escolhido: **Clima local - chuva intensa e risco de alagamento**

## Integrantes

| Gabriel Ramos Moreira| RM564074 |
| Vinicius Mello Siqueira| RM565257 |
| 

## Sobre o projeto

Sistema de console em Java que monitora um ponto do bairro que costuma alagar (o trecho baixo de um córrego). Três sensores simulados (chuva, nível do canal e umidade do solo) fazem leituras, tudo é gravado em um banco H2, e com base no histórico o sistema faz previsões e emite alertas automáticos de **ATENÇÃO**, **ALERTA** ou **CRÍTICO**, explicando o motivo de cada um.

### Público beneficiado

Quem sofre com o problema são os moradores das áreas baixas perto do córrego, principalmente quem mora em casas térreas e perde móveis e documentos toda vez que a água sobe. O sistema é pensado para a **associação de moradores / equipe comunitária de plantão** e para a **Defesa Civil local**.

A decisão que ele apoia é: *"preciso mandar alguém vistoriar o córrego, abrir o ponto de apoio (escola do bairro) ou avisar os moradores agora, ou dá para esperar?"*. Hoje essa decisão costuma ser tomada quando a água já está na rua; a ideia é antecipar algumas horas usando as leituras.

O território é descrito só por região e equipamentos públicos (escola de referência, população estimada). **Não guardamos nenhum dado pessoal** (nome, CPF, endereço).

## Como executar

### Pré-requisitos

- JDK 17 ou superior
- Eclipse com Maven (m2e), que já vem no "Eclipse IDE for Java Developers", **ou** Maven 3.9+ no terminal
- Internet na primeira vez, para o Maven baixar o H2, o JUnit e o Mockito

Não precisa instalar nenhum banco. O H2 vem como dependência do Maven e as tabelas são criadas sozinhas quando o programa abre.

### Pelo Eclipse

1. `File > Import > Maven > Existing Maven Projects`
2. Selecione a pasta `territory-predictive-insights-fev` e clique em **Finish**
3. Espere o Maven baixar as dependências (barra de progresso no canto inferior)
4. Abra `src/main/java/br/com/fiap/territorio/SistemaMonitoramento.java`
5. Botão direito > `Run As > Java Application`
6. Para os testes: botão direito na pasta `src/test/java` > `Run As > JUnit Test`

### Pelo terminal

```bash
mvn clean test      # compila e roda os testes
mvn compile exec:java   # abre o menu
```

Os comandos precisam ser rodados **na pasta do projeto** (onde fica o `pom.xml`).

### Banco de dados

- Usamos **H2 em arquivo** (`jdbc:h2:./dados/territorio`), e não em memória, porque um dos requisitos é que os dados continuem lá depois de reiniciar o programa.
- O arquivo `dados/territorio.mv.db` é criado na primeira execução. As tabelas `medicao` e `alerta` são criadas pela classe `ConexaoBanco` com `CREATE TABLE IF NOT EXISTS`.
- Usuário `sa`, sem senha (é só para o trabalho).
- Para começar com o banco zerado, é só apagar a pasta `dados/`.
- Se aparecer erro de banco "travado", provavelmente o programa está aberto em outra janela/console do Eclipse. Feche a outra execução (botão vermelho do console).

## Menu

```
1 - Verificar sensores (fazer leituras)
2 - Rodar previsões
3 - Ver histórico de medições por sensor
4 - Ver alertas emitidos
5 - Gerenciar dados do território
6 - Status completo
7 - Registrar leitura manual
8 - Exportar último histórico consultado (CSV)
0 - Sair
```

- **1**: mostra o status dos sensores e pergunta quantas rodadas de leitura fazer (cada rodada lê os 3 sensores). Isso ajuda a juntar histórico rápido para a previsão. Leituras com problema aparecem como `[FALHA]` ou `[DESCARTADA]` e não são gravadas.
- **2**: escolhe a estratégia (ou usa a padrão da configuração) e roda a previsão para os 3 sensores com o histórico do banco. Se passar de algum limiar, o alerta é gravado e explicado.
- **5**: altera região, equipamento, população, liga/desliga sensor, troca a estratégia padrão e altera os limiares. **Os limiares só mudam com o código de acesso. O código inicial é `GS2026`.**
- **7**: serve para registrar uma medição feita por alguém da equipe no local. Também é a forma mais fácil de testar a validação (ex: digitar `-5` de chuva).

### Roteiro rápido para testar

1. Opção **2**: sem leituras, o sistema não faz previsão e explica o motivo.
2. Opção **1** com **10** rodadas: grava ~30 leituras (às vezes aparece uma falha simulada).
3. Opção **2** com a estratégia 1 e depois com a 2: resultados diferentes para o mesmo histórico, e os alertas aparecem com o motivo.
4. Opções **3** e **4**: histórico e alertas salvos. Depois a opção **8** exporta o histórico consultado.
5. Opção **7** com valor `-5`: aparece `DadosInvalidosException`.
6. Opção **5 > 5** para desligar um sensor, depois a opção **1**: aparece `FonteIndisponivelException` para aquele sensor.
7. Sair com **0**, rodar de novo e abrir as opções **3** e **4**: os dados continuam lá.

## Estrutura

```
territory-predictive-insights-fev/
├── pom.xml
├── README.md
└── src/
    ├── main/java/br/com/fiap/territorio/
    │   ├── SistemaMonitoramento.java        (main + menu)
    │   ├── dominio/
    │   │   ├── ComponenteTerritorial.java   (abstrata)
    │   │   ├── PontoMonitoramento.java      (filha concreta)
    │   │   ├── Sensor.java                  (interface)
    │   │   ├── SensorSimulado.java          (abstrata, base dos sensores)
    │   │   ├── SensorChuva.java / SensorNivelCanal.java / SensorUmidadeSolo.java
    │   │   ├── DadosTerritorio.java
    │   │   ├── EstrategiaPrevisao.java      (abstrata)
    │   │   ├── PrevisaoMediaMovel.java / PrevisaoComparacao.java
    │   │   ├── Medicao.java / Alerta.java / Limiar.java / ResultadoPrevisao.java
    │   │   └── NivelAlerta.java / TipoSensor.java / TipoPrevisao.java / StatusComponente.java
    │   ├── excecao/
    │   │   ├── DadosInvalidosException.java
    │   │   └── FonteIndisponivelException.java
    │   ├── persistencia/
    │   │   ├── ConexaoBanco.java
    │   │   ├── MedicaoDAO.java
    │   │   └── AlertaDAO.java
    │   └── servico/
    │       ├── ServicoMonitoramento.java
    │       ├── ServicoAlerta.java
    │       ├── FabricaPrevisao.java         (Factory)
    │       ├── ConfiguracaoSistema.java     (Singleton)
    │       └── ExportadorCsv.java           (bônus)
    └── test/java/br/com/fiap/territorio/
        ├── dominio/  MedicaoTest, PrevisaoMediaMovelTest, PrevisaoComparacaoTest, DadosTerritorioTest
        └── servico/  ServicoMonitoramentoTest, ServicoAlertaTest, FabricaPrevisaoTest, ExportadorCsvTest
```

## Conceitos de POO aplicados

**Abstração e herança:** `ComponenteTerritorial` é abstrata (id, nome, região, status) e tem o método abstrato `verificarStatus()`. A filha concreta direta é `PontoMonitoramento`, que agrupa os sensores e diz quantos estão funcionando. Os sensores também herdam dela através de `SensorSimulado`.

**Interface e polimorfismo:** a interface `Sensor` tem `lerValor()`, `verificarFuncionamento()` e `retornarTipo()`. São 3 implementações: `SensorChuva`, `SensorNivelCanal` e `SensorUmidadeSolo`. Cada uma gera o valor do seu jeito: a chuva quase sempre é fraca mas às vezes vem um temporal, e o canal nunca fica seco. O `ServicoMonitoramento` e o `ServicoAlerta` só conhecem a interface `Sensor`.

**Encapsulamento:** todos os atributos são privados e as validações ficam nos setters/construtores. Em `DadosTerritorio`, `setPopulacaoEstimada(-10)` lança exceção, e `alterarLimiares(...)` só funciona com o código de acesso certo.

**Estratégias de previsão:** `EstrategiaPrevisao` é abstrata e as duas filhas sobrescrevem `prever()`. Os construtores chamam `super(nome, minimoLeituras)` e o `prever()` de cada uma chama `super.validarHistorico(...)` antes de calcular. Quem usa a estratégia não sabe qual é, só chama `estrategia.prever(historico)`. Não tem `instanceof` nem `if (tipo == ...)` para escolher qual rodar.

## Previsões

Não é estatística avançada, como o enunciado pede. São duas contas simples sobre o histórico do banco:

| Estratégia | Como calcula | Mínimo de leituras |
|---|---|---|
| **Média móvel** (`PrevisaoMediaMovel`) | média das 5 últimas leituras do sensor | 5 |
| **Comparação** (`PrevisaoComparacao`) | média das 3 últimas comparada com a média das 3 anteriores. Previsão = média recente + variação (se está subindo, a tendência é continuar subindo). Nunca fica negativa. | 6 |

Exemplo do nível do canal com `90, 100, 110, 150, 160, 170`:
- Média móvel: (100+110+150+160+170)/5 = **138 cm**, abaixo de ATENÇÃO, sem alerta
- Comparação: anterior 100, recente 160, variação +60, previsão 160+60 = **220 cm**, nível ALERTA

As duas olham para o mesmo histórico de jeitos diferentes. A média é mais "calma", a comparação reage mais rápido quando a água começa a subir. Esse exemplo está no teste `estrategiasDiferentesDevemDarResultadosDiferentes`.

## Alertas

Os limiares vêm do `ConfiguracaoSistema`. Os valores iniciais são didáticos (não são oficiais) e podem ser alterados pela opção 5 com o código de acesso:

| Sensor | Unidade | ATENÇÃO | ALERTA | CRÍTICO |
|---|---|---|---|---|
| Chuva | mm/h | 10 | 25 | 50 |
| Nível do canal | cm | 150 | 220 | 300 |
| Umidade do solo | % | 70 | 85 | 95 |

- Previsão **igual ou maior** que o limite já entra no nível (ex: chuva de 25 mm/h é ALERTA).
- Abaixo de ATENÇÃO, o sistema não emite nem grava alerta.
- Cada alerta guarda e mostra: o sensor, o valor previsto, o limite que foi ultrapassado, a estratégia usada e uma sugestão de ação para a equipe. Exemplo:

```
!!! [ALERTA] 17/11/2026 16:36:17
    Por quê: Sensor Régua do canal R-01 (Nível do canal): previsão de 260,62 cm pela Média móvel atingiu o limite de ALERTA (220,00 cm).
    Sugestão para a equipe: Enviar alguém para vistoriar o ponto e preparar o abrigo de apoio.
```

## Exceções

| Exceção | Quando é lançada |
|---|---|
| `DadosInvalidosException` | medição negativa ou fora da faixa do sensor (ex: umidade 130%), NaN, população negativa, região vazia, limiares fora de ordem (ATENÇÃO >= ALERTA), código de acesso errado, histórico insuficiente para prever, estratégia que não existe |
| `FonteIndisponivelException` | sensor desligado ou falha de comunicação simulada (3% de chance a cada leitura) |

As duas são checked (`extends Exception`), então o compilador obriga a tratar. Elas são lançadas nos setters (`Medicao.setValor`, `DadosTerritorio.setPopulacaoEstimada`...) e nos serviços, e são capturadas no menu com uma mensagem que explica o que aconteceu. Não tem nenhum `catch` vazio. Na leitura dos sensores cada sensor tem seu próprio `try/catch`, então se um falhar os outros continuam lendo. Também simulamos 3% de leituras com defeito (valor negativo), que são barradas pela validação da `Medicao` e não chegam ao banco.

## Persistência (JDBC)

Escolhemos o caminho **JDBC** porque já tínhamos feito DAO com `PreparedStatement` na matéria e preferimos deixar o projeto mais leve, sem Spring.

- `MedicaoDAO`: `salvar`, `listarPorSensor` (ordenado por data) e `contarPorSensor`
- `AlertaDAO`: `salvar`, `listarTodos` e `contar`
- Todo SQL usa `PreparedStatement` com `?`. Nenhum valor é concatenado na String do SQL.
- `Connection`, `PreparedStatement` e `ResultSet` são abertos com **try-with-resources**, então fecham sozinhos mesmo se der erro.
- Toda medição válida e todo alerta emitido são gravados na hora. A previsão sempre lê o histórico do banco, não de uma lista em memória.

## Padrões criacionais

### Factory - `FabricaPrevisao`

**Por que ali:** a estratégia de previsão pode vir de dois lugares: da escolha do usuário no menu ou da configuração padrão (`ConfiguracaoSistema`). Sem a fábrica, o menu e o teste teriam que saber quais classes existem e fazer `if (escolha == 1) new PrevisaoMediaMovel()...` em cada lugar. A fábrica guarda um `Map<TipoPrevisao, Supplier<EstrategiaPrevisao>>` e só devolve o objeto pronto como `EstrategiaPrevisao`. Para criar uma terceira estratégia é só registrar mais uma linha no construtor da fábrica, sem mexer no menu nem nos serviços.

**Por que não em outro lugar:** os sensores e o `DadosTerritorio` são criados uma vez só, no início do programa, com dados fixos. Uma fábrica para eles seria código a mais sem ganho nenhum.

### Singleton - `ConfiguracaoSistema`

**Por que ali:** os limiares precisam ser os mesmos para quem altera (`DadosTerritorio`, na opção 5) e para quem compara (`ServicoAlerta`). Se cada um tivesse sua própria instância, a pessoa alteraria o limiar e o alerta continuaria usando o antigo. Com construtor privado e `getInstancia()`, existe uma configuração só no programa todo. Ela também guarda a estratégia padrão que a `FabricaPrevisao` usa.

**Por que não em outro lugar:** não faz sentido Singleton para DAO ou serviço, porque nos testes a gente precisa criar o serviço com um DAO falso (mock). Se fossem Singletons, isso ficaria bem mais difícil.

O código sempre chama `fabricaPrevisao.criar(...)` / `criarPadrao()` e `ConfiguracaoSistema.getInstancia()`. Não existe nenhum `new PrevisaoMediaMovel()` no menu.

## Testes

São **30 testes** JUnit, todos no padrão **AAA** (Arrange, Act, Assert, marcado com comentário em cada teste). Eles não abrem o banco nem dependem de digitação no console.

| Classe de teste | O que verifica |
|---|---|
| `MedicaoTest` | medição válida, chuva negativa (`assertThrows`), umidade acima de 100%, setter com NaN não altera o valor |
| `PrevisaoMediaMovelTest` | média das 5 últimas (ignora as mais antigas), menos de 5 leituras lança exceção, não mistura sensores |
| `PrevisaoComparacaoTest` | projeção de alta (220 cm), queda forte não fica negativa, precisa de 6 leituras, as duas estratégias dão resultados diferentes |
| `DadosTerritorioTest` | população negativa, limiar com código certo/errado, limiares fora de ordem |
| `FabricaPrevisaoTest` | cria cada estratégia pelo nome, usa a padrão da configuração, nome desconhecido, Singleton devolve a mesma instância |
| `ServicoAlertaTest` (**Mockito**) | `AlertaDAO` mockado: previsão 62 vira CRÍTICO e é salva, 25 (exatamente no limite) vira ALERTA, 9,99 não gera nem salva alerta, limiar alterado é respeitado |
| `ServicoMonitoramentoTest` (**Mockito**) | `MedicaoDAO` e `Sensor` mockados: leitura válida é salva, leitura negativa não é salva, sensor desligado e falha de comunicação lançam `FonteIndisponivelException`, previsão usa o histórico que veio do DAO |
| `ExportadorCsvTest` | CSV com cabeçalho e uma linha por medição, histórico vazio não exporta |

Os testes têm valores esperados calculados na mão (ex: `(10+20+30+40+50)/5 = 30`). Se alguém trocar o `>=` por `>` no `ServicoAlerta`, o teste do limite exato fica vermelho.

## Bônus - Exportação CSV

Depois de consultar o histórico de um sensor (opção 3), a opção 8 exporta **exatamente aquele histórico** para `exportacoes/historico_sensor_<id>_<data>.csv`, separado por `;` e com vírgula decimal para abrir direto no Excel.

## Decisões da equipe

- **Recorte de alagamento**: foi o que mais fez sentido para a gente, porque é um problema que aparece todo ano no noticiário de São Paulo e dá para medir com sensores simples (pluviômetro, régua no canal e umidade do solo).
- **JDBC em vez de Spring**: menos configuração e o professor consegue rodar só com Run As > Java Application.
- **H2 em arquivo**: em memória perderia tudo ao fechar, e o enunciado pede que os dados sobrevivam ao reiniciar.
- **"Rodadas" de leitura na opção 1**: para não precisar apertar a opção 10 vezes até ter histórico suficiente para a previsão.
- **Falhas simuladas nos sensores**: sem elas as exceções nunca apareceriam usando o sistema de verdade, só nos testes.
- **Leitura manual**: além de testar a validação, representa uma situação real (alguém da equipe mede o nível com régua quando o sensor cai).
- **Limiares em memória**: os limiares alterados valem até fechar o programa. O enunciado pede persistência de medições e alertas, então deixamos a configuração voltar ao padrão para não complicar.

## Reflexão crítica

**Quando o sistema deve se calar?** Decidimos que, com poucas leituras, o sistema não faz previsão nenhuma em vez de "chutar". A média móvel precisa de no mínimo 5 leituras e a comparação de 6 (dois períodos de 3). Com 2 ou 3 leituras, uma única chuva forte isolada já levaria a média lá para cima e geraria um alerta CRÍTICO falso. No status completo o sistema avisa quando um sensor ainda tem menos de 5 leituras. Ele também fica quieto quando a previsão está abaixo do limite de ATENÇÃO: mostra o valor, mas não grava alerta. Se o sistema alertar toda hora, as pessoas param de acreditar nele, e aí o alerta que importa é ignorado.

**O que os dados não representam?** Temos um sensor de cada tipo para a região inteira. Um pluviômetro mede a chuva que cai em cima dele, e a rua de baixo pode encher por causa da chuva que caiu no bairro vizinho, rio acima. O sistema também não sabe se o bueiro está entupido de lixo, se a encosta tem casas, se é uma rua asfaltada ou de terra, nem quantas pessoas moram exatamente na parte que alaga (a população é só uma estimativa). Além disso os valores são simulados com `Random` e os limiares são números que escolhemos para o trabalho, não dados oficiais da Defesa Civil. Um sistema de verdade precisaria de mais sensores, calibração e histórico real (por exemplo do INMET ou do CGE).

**Por que o alerta apoia a decisão em vez de decidir?** Porque o sistema erra, e as duas formas de errar são graves: um falso alarme faz famílias saírem de casa à toa, e um "está tudo bem" errado deixa as pessoas desprevenidas. Quem está na equipe conhece o bairro, sabe que tal rua sempre enche primeiro e consegue ir lá olhar. Por isso cada alerta mostra o motivo (sensor, valor previsto, limite ultrapassado e estratégia) e uma *sugestão* de ação, não uma ordem. Quem decide avisar os moradores ou acionar a Defesa Civil é uma pessoa, e a responsabilidade continua sendo dela.



## Uso de IA

Usamos IA generativa (Claude) como apoio durante o desenvolvimento, principalmente para:
1- tirar dúvidas sobre como mockar o DAO com Mockito e testar exceções com `assertThrows`;
2- entender o erro de banco travado do H2 ao abrir o programa duas vezes;
3- revisar o projeto total comparando se teve erros e os corrigindo.
4- Nos ajudar nas boas praticas de codigo, separar corretamente cada path, arquivo, erros de codigo, pontos e virgulas, para otimizar tempo!!


