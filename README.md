# Freight Aggregator

API para cotação de frete, que consulta múltiplas transportadoras em paralelo, aplica margem de lucro da plataforma e retorna as opções ordenadas por preço.

Projeto construído seguindo os princípios de **Arquitetura Hexagonal (Ports & Adapters)**.

---


## Conceito: Arquitetura Hexagonal

A Arquitetura Hexagonal (também chamada de **Ports & Adapters**), proposta por Alistair Cockburn, tem como objetivo principal **isolar a regra de negócio de detalhes técnicos** como framework web, banco de dados, ou integrações externas.

A ideia central é: o **domínio** (a lógica de negócio pura) fica no centro da aplicação e não depende de nada externo nem de Spring, nem de JPA, nem de HTTP. Tudo que é "detalhe de infraestrutura" se conecta ao domínio através de **interfaces** chamadas de **ports** (portas).

Isso traz algumas vantagens práticas:

- **Testabilidade**: a lógica de negócio pode ser testada sem subir um banco de dados ou um servidor web.
- **Flexibilidade**: trocar PostgreSQL por MongoDB, ou adicionar uma nova transportadora, não exige alterar a regra de negócio só criar um novo *adapter*.
- **Baixo acoplamento**: o domínio não sabe (e não precisa saber) como os dados chegam até ele ou para onde vão depois.

### Ports e Adapters

- **Port (porta)**: uma interface que define um contrato. Existem dois tipos:
  - **Port de entrada (`in`)**: define o que o mundo externo pode pedir para a aplicação fazer (ex: "calcular um frete").
  - **Port de saída (`out`)**: define o que a aplicação precisa do mundo externo (ex: "salvar uma cotação", "consultar uma transportadora").
- **Adapter (adaptador)**: a implementação concreta de um port. É quem sabe os detalhes técnicos.
  - **Adapter de entrada**: traduz uma requisição externa (HTTP, mensageria, CLI) em uma chamada para o caso de uso. Ex: um `@RestController`.
  - **Adapter de saída**: implementa o port de saída usando uma tecnologia concreta. Ex: um repositório JPA, ou um cliente HTTP para uma API de transportadora.


---

## Estrutura do projeto

```
src/main/java/com/guilherme/freight_aggregator/
│
├── domain/                          → Regra de negócio pura (sem dependências externas)
│   ├── model/
│   │   ├── Address.java             → Value Object: endereço (com validação de CEP)
│   │   ├── PackageDimension.java    → Value Object: dimensões e peso do pacote
│   │   ├── FreightOption.java       → Value Object: uma opção de frete cotada
│   │   └── FreightQuote.java        → Entidade/Agregado: a cotação completa
│   ├── service/
│   │   └── FreightCalculatorDomainService.java  → Regra de negócio: margem + ordenação
│   └── exception/
│       └── DomainException.java     → Exceção de domínio
│
├── application/                     → Casos de uso e contratos (ports)
│   ├── ports/
│   │   ├── in/
│   │   │   ├── CalculateFreightUseCase.java     → Port de entrada (contrato do caso de uso)
│   │   │   └── CalculateFreightCommand.java     → DTO interno de entrada
│   │   └── out/
│   │       ├── CarrierIntegrationPort.java      → Port de saída (contrato p/ transportadoras)
│   │       └── FreightRepositoryPort.java       → Port de saída (contrato de persistência)
│   └── usecases/
│       └── CalculateFreightService.java         → Implementação do caso de uso (orquestrador)
│
└── infrastructure/                  → Adapters (implementações concretas / detalhes técnicos)
    ├── adapters/
    │   ├── in/
    │   │   └── web/
    │   │       ├── FreightController.java       → Adapter de entrada (REST)
    │   │       ├── FreightRequestDTO.java        → DTO de entrada HTTP
    │   │       └── FreightReponse.java           → DTO de saída HTTP
    │   └── out/
    │       ├── carriers/
    │       │   ├── JadlogAdapter.java            → Adapter de saída: transportadora Jadlog
    │       │   └── CorreiosAdapter.java          → Adapter de saída: transportadora Correios
    │       └── persistence/
    │           ├── CotacaoFreteJpaEntity.java        → Entidade JPA (tabela do banco)
    │           ├── SpringDadosFreteRepository.java   → Repositório Spring Data JPA
    │           └── PostgresFreightRepositoryAdapter.java → Adapter de saída: persistência
    └── config/
        └── BeanConfiguration.java   → Configuração manual de beans (injeção de dependência)
```

---

## Camada Domain

É o núcleo do sistema. Não importa nenhuma classe do Spring, do JPA ou de HTTP  só Java puro.

### `Address` (Value Object)

```java
public record Address(String cep, String rua, String cidade, String estado) {
    public Address {
        if (cep == null || !cep.matches("\\d{5}-?\\d{3}")) {
            throw new IllegalArgumentException("CEP inválido. Deve conter 8 dígitos.");
        }
    }
}
```

Representa um endereço. A validação acontece **no construtor** (`compact constructor` de record)  isso garante que, se um `Address` existe na memória, ele necessariamente é válido. É impossível criar um `Address` com CEP inválido em nenhum ponto do sistema.

O CEP aceita os formatos `NNNNNNNN` ou `NNNNN-NNN`.

### `PackageDimension` (Value Object)

```java
public record PackageDimension(double larguraCm, double alturaCm, double comprimentoCm, double pesoKg) {
    public PackageDimension {
        if (larguraCm <= 0 || alturaCm <= 0 || comprimentoCm <= 0 || pesoKg <= 0) {
            throw new IllegalArgumentException("Dimensões e peso devem ser maiores que zero");
        }
    }

    public double obterPesoCubico() {
        return (larguraCm * alturaCm * comprimentoCm) / 6000.0;
    }

    public double obterPesoEfetivo() {
        return Math.max(pesoKg, obterPesoCubico());
    }
}
```

Guarda as dimensões físicas do pacote e implementa o cálculo de **peso cubado**  uma regra padrão do mercado de logística: pacotes grandes porém leves ocupam espaço no transporte proporcional ao seu volume, não ao seu peso real. Por isso as transportadoras cobram pelo maior valor entre peso real e "peso equivalente ao volume".

- `obterPesoCubico()`: calcula o peso equivalente ao volume, usando o fator de cubagem `6000` (padrão do setor para transporte rodoviário nacional).
- `obterPesoEfetivo()`: retorna o **maior** valor entre o peso real e o peso cubado é esse valor que entra nas fórmulas de preço de cada transportadora.

### `FreightOption` (Value Object)

```java
public record FreightOption(String nomeTransportadora, String nomeServico, double preco, int diasEntrega) {
    public FreightOption comMargem(double percentualMargem) {
        double novoPreco = this.preco * (1 + (percentualMargem / 100.0));
        return new FreightOption(this.nomeTransportadora, this.nomeServico,
                Math.round(novoPreco * 100.0) / 100.0, this.diasEntrega);
    }
}
```

Representa uma opção de frete já cotada por uma transportadora. O método `comMargem()` aplica um percentual de margem sobre o preço bruto e retorna uma **nova** instância (imutabilidade  não altera o objeto original), arredondando o resultado para 2 casas decimais.

### `FreightQuote` (Entidade / Agregado)

Representa a cotação completa gerada para o usuário: um `id` (UUID), origem, destino, dimensões do pacote, a lista final de opções processadas, e o timestamp de criação. É o **agregado raiz** do domínio  o objeto que amarra tudo o que pertence a uma única cotação.

### `FreightCalculatorDomainService`

```java
private static final double Porcentagem_Margem_Plataforma = 10.0;

public List<FreightOption> ProcessarClassificarOpcoes(List<FreightOption> opcoes) {
    if (opcoes == null || opcoes.isEmpty()) return List.of();

    return opcoes.stream()
            .map(option -> option.comMargem(Porcentagem_Margem_Plataforma))
            .sorted(Comparator.comparingDouble(FreightOption::preco))
            .toList();
}
```

Contém a regra de negócio de como **combinar** as cotações recebidas das transportadoras:
1. Aplica **10% de margem da plataforma** sobre o preço bruto de cada opção.
2. Ordena as opções da mais barata para a mais cara.

---

## Camada Application

Orquestra o fluxo de negócio e define os contratos (ports) que conectam o domínio ao mundo externo. Não contém regra de negócio em si  só coordena.

### Ports de entrada (`ports/in`)

- **`CalculateFreightUseCase`**: interface que expõe a operação `calculate(CalculateFreightCommand)`. É o contrato que qualquer adapter de entrada (REST, CLI, mensageria) usa para acionar o caso de uso, sem precisar saber como ele é implementado por dentro.
- **`CalculateFreightCommand`**: DTO interno que carrega os dados necessários para calcular um frete, independente de como a requisição chegou (JSON, formulário, etc).

### Ports de saída (`ports/out`)

- **`CarrierIntegrationPort`**: contrato que toda integração com transportadora precisa implementar:
  ```java
  Optional<FreightOption> calcularTaxa(Address origem, Address destino, PackageDimension dimensaoPacote);
  ```
  Retorna `Optional.empty()` quando a transportadora falha ou está indisponível  isso permite que o sistema continue funcionando mesmo se uma integração cair.

- **`FreightRepositoryPort`**: contrato de persistência (`save`, `findById`), sem expor qual tecnologia de banco está por trás.

### `CalculateFreightService` (caso de uso)

É o orquestrador central. Implementa `CalculateFreightUseCase` e depende **apenas** de interfaces (ports), nunca de implementações concretas  isso é o que caracteriza a **Inversão de Dependência** na prática.

Fluxo de `calculate()`:

1. Converte o `Command` recebido em objetos de domínio (`Address`, `PackageDimension`)  é aqui que as validações do domínio disparam, caso os dados estejam incorretos.
2. Chama **todas** as transportadoras disponíveis (injetadas como `List<CarrierIntegrationPort>`), filtrando automaticamente as que falharam:
   ```java
   List<FreightOption> opcoes = portasOperadora.stream()
           .map(operadora -> operadora.calcularTaxa(origem, destino, dimensao))
           .flatMap(Optional::stream)
           .toList();
   ```
3. Delega ao `FreightCalculatorDomainService` a aplicação de margem e ordenação.
4. Monta o `FreightQuote` (agregado final, com novo id e timestamp).
5. Persiste via `FreightRepositoryPort` e retorna o resultado.

---

## Camada Infrastructure

Contém todos os *adapters* as implementações concretas que conectam o domínio ao mundo real (web, banco de dados, integrações externas).

### Adapter de entrada  Web (`adapters/in/web`)

- **`FreightController`**: expõe o endpoint `POST /freights/calculate`. Converte o JSON de entrada (`FreightRequestDTO`) em `CalculateFreightCommand`, aciona o caso de uso, e converte o resultado (`FreightQuote`) de volta para JSON de saída (`FreightReponse`). É a **única** camada da aplicação que sabe que existe HTTP.
- **`FreightRequestDTO`**: DTO de entrada, com validações via Bean Validation (`@NotBlank`, `@Positive`) validações de **formato de requisição**, diferentes das validações de **regra de negócio** que ficam no domínio.
- **`FreightReponse`**: DTO de saída, formatado especificamente para o consumidor da API.

### Adapters de saída  Transportadoras (`adapters/out/carriers`)

- **`JadlogAdapter`**: simula a cotação da Jadlog. `preco = 15.0 + (pesoEfetivo * 6.0)`, prazo fixo de 2 dias.
- **`CorreiosAdapter`**: simula a cotação dos Correios (SEDEX). `preco = 20.0 + (pesoEfetivo * 4.5)`, prazo fixo de 5 dias.

Ambas as classes são **simulações** em produção, o método `calcularTaxa` faria uma chamada HTTP real para a API da transportadora correspondente. Isso é a maior vantagem prática da arquitetura hexagonal: trocar a simulação por uma chamada HTTP real não exige mudar nenhuma linha do domínio ou do caso de uso só o conteúdo do adapter.

> ⚠️ Atenção: apenas `JadlogAdapter` possui a anotação `@Component`. Sem essa anotação, o Spring **não registra** a classe como bean, e ela não é injetada na lista `List<CarrierIntegrationPort>` do caso de uso  ou seja, hoje o `CorreiosAdapter` existe no código mas não participa das cotações.

### Adapters de saída  Persistência (`adapters/out/persistence`)

- **`CotacaoFreteJpaEntity`**: a entidade JPA mapeada para a tabela `cotacao_frete` no PostgreSQL.
- **`SpringDadosFreteRepository`**: interface `JpaRepository<CotacaoFreteJpaEntity, String>`  o Spring Data gera a implementação automaticamente (métodos `save`, `findById`, etc.).
- **`PostgresFreightRepositoryAdapter`**: implementa `FreightRepositoryPort`, fazendo a ponte entre o modelo de domínio (`FreightQuote`) e a entidade JPA (`CotacaoFreteJpaEntity`)  converte um no outro na hora de persistir. É essa camada que impede o domínio de conhecer JPA/Hibernate.

### Configuração (`infrastructure/config`)

- **`BeanConfiguration`**: monta manualmente os beans do caso de uso e do serviço de domínio, injetando **automaticamente** todos os beans que implementam `CarrierIntegrationPort` (Spring detecta qualquer `@Component` desse tipo) e o `FreightRepositoryPort`. É essa injeção de lista que permite "chamar todas as transportadoras" sem hardcode de quais existem.

---


## Regras de negócio

| Regra | Onde está | Detalhe |
|---|---|---|
| CEP deve ter 8 dígitos (com ou sem hífen) | `Address` | `\d{5}-?\d{3}` |
| Dimensões e peso devem ser > 0 | `PackageDimension` | valida na criação |
| Peso cubado | `PackageDimension.obterPesoCubico()` | `(largura × altura × comprimento) / 6000` |
| Peso efetivo (o que entra no cálculo de preço) | `PackageDimension.obterPesoEfetivo()` | `max(peso real, peso cubado)` |
| Preço Jadlog | `JadlogAdapter` | `15.0 + (pesoEfetivo × 6.0)`, prazo 2 dias |
| Preço Correios (SEDEX) | `CorreiosAdapter` | `20.0 + (pesoEfetivo × 4.5)`, prazo 5 dias |
| Margem da plataforma | `FreightCalculatorDomainService` | +10% sobre o preço bruto de cada opção |
| Ordenação das opções | `FreightCalculatorDomainService` | crescente por preço final |
| Falha de uma transportadora não derruba a cotação | `CalculateFreightService` | `Optional.empty()` é descartado via `flatMap(Optional::stream)` |

---

## Modelo de dados persistido

Tabela `cotacao_frete` (criada via migration Flyway `V1__create_cotacao_frete_table.sql`):

| Coluna | Tipo | Origem |
|---|---|---|
| `id` | String (UUID) | Gerado em `FreightQuote` na criação |
| `cep_origem` | String | CEP de origem informado |
| `cep_destino` | String | CEP de destino informado |
| `peso_kg` | double | Peso real informado (não o peso cubado) |
| `opcoes_total` | int | Quantidade de opções de frete retornadas pelas transportadoras |
| `criado_em` | LocalDateTime | Timestamp da criação da cotação |

> Nota: atualmente o banco guarda apenas um **resumo** da cotação (quantidade de opções, não o detalhe de preço/prazo de cada transportadora). Ver [Limitações conhecidas](#limitações-conhecidas--próximos-passos).

## Como rodar o projeto

1. Configure as variáveis de ambiente `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` (via `.env` ou variáveis de sistema).
2. Suba um banco PostgreSQL local (ex: `frete_db`).
3. Rode:
   ```bash
   mvnw.cmd clean package -DskipTests
   mvnw.cmd spring-boot:run
   ```
4. O Flyway executa a migration automaticamente e cria a tabela `cotacao_frete`.

