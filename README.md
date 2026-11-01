# Time App

Aplicação de estudo criada com **Quarkus** para explorar conceitos de resiliência e observabilidade em APIs, especialmente usando recursos do **MicroProfile Fault Tolerance** e **MicroProfile Health**.

## Objetivo

Este projeto tem como objetivo estudar, de forma prática:

1. O uso de **Fallback**;
2. O padrão **Circuit Breaker**;
3. Os checks de saúde **Liveness** e **Readiness** no Quarkus.

A aplicação consome uma API externa de horário e aplica mecanismos de tolerância a falhas para lidar com indisponibilidade, lentidão ou erros nesse serviço externo.

## Tecnologias utilizadas

- Java
- Quarkus
- Jakarta REST
- MicroProfile Rest Client
- MicroProfile Fault Tolerance
- MicroProfile Health

## Conceitos estudados

## Fallback

O **Fallback** é um mecanismo usado para fornecer uma resposta alternativa quando uma chamada falha.

Ele é útil quando a aplicação depende de um serviço externo que pode estar:

- Fora do ar;
- Muito lento;
- Retornando erro;
- Indisponível temporariamente.

Em vez de deixar a falha quebrar completamente o fluxo da aplicação, o fallback permite retornar uma resposta padrão ou executar uma lógica alternativa.

### Para que serve?

O fallback serve para aumentar a resiliência da aplicação.

Exemplo de uso:

- Uma API de cotação está fora do ar;
- Uma API de horário não responde;
- Um serviço de pagamento está temporariamente indisponível;
- Uma chamada externa ultrapassa o tempo limite configurado.

Nesses casos, a aplicação pode retornar uma resposta alternativa, como:
``Fallback: 2026-11-01T10:00:00``

### Como usar

No MicroProfile Fault Tolerance, o fallback pode ser usado com a anotação:
``java @Fallback(fallbackMethod = "nomeDoMetodoFallback")``


Exemplo:
```java
    @Fallback(fallbackMethod = "getTimeFallback") 
    public String getTime();
    default String getTimeFallback() { 
        return "Fallback: " + LocalDateTime.now(); 
    }
```

Quando o método principal falhar, o método definido em `fallbackMethod` será chamado.

### Opções comuns

Algumas formas comuns de usar fallback são:

- Retornar um valor padrão;
- Retornar uma mensagem amigável;
- Buscar dados em cache;
- Chamar outro serviço alternativo;
- Retornar uma resposta simplificada;
- Registrar logs para análise posterior.

### Cuidados ao usar Fallback

Apesar de ser muito útil, o fallback deve ser usado com cuidado.

Ele não deve esconder problemas graves da aplicação. Se o fallback for usado sempre, pode parecer que está tudo funcionando, quando na verdade o serviço principal está indisponível.

Por isso, é comum combinar fallback com:

- Logs;
- Métricas;
- Health checks;
- Alertas;
- Circuit Breaker.

## Circuit Breaker

O **Circuit Breaker** é um padrão de resiliência usado para evitar chamadas repetidas para um serviço que está falhando.

Ele funciona como um disjuntor elétrico: quando muitas falhas acontecem, o circuito é aberto e novas chamadas deixam de ser feitas temporariamente.

Isso evita que a aplicação continue tentando acessar um serviço indisponível e piore ainda mais o problema.

### Para que serve?

O Circuit Breaker serve para:

- Proteger a aplicação contra falhas em cascata;
- Evitar sobrecarga em serviços externos;
- Reduzir tempo de espera em chamadas que provavelmente falhariam;
- Permitir recuperação gradual de serviços instáveis;
- Melhorar a experiência do usuário em cenários de falha.

### Estados do Circuit Breaker

O Circuit Breaker geralmente trabalha com três estados:

### Closed

Estado normal.

As chamadas são feitas normalmente para o serviço externo.

Se muitas chamadas começarem a falhar, o circuito pode mudar para o estado `Open`.

### Open

Estado de circuito aberto.

As chamadas deixam de ser enviadas para o serviço externo por um tempo determinado.

Nesse período, a aplicação pode acionar diretamente o fallback.

### Half-Open

Estado intermediário.

Depois de um tempo de espera, o Circuit Breaker permite algumas chamadas de teste.

Se essas chamadas forem bem-sucedidas, o circuito volta para `Closed`.

Se falharem, o circuito volta para `Open`.

### Como usar

No MicroProfile Fault Tolerance, o Circuit Breaker pode ser usado com a anotação:

```java 
@CircuitBreaker(
            requestVolumeThreshold = 4,
            failureRatio = 0.5,
            delay = 5000L,
            successThreshold = 2
)
```


### Opções comuns

As opções mais comuns são:

### `requestVolumeThreshold`
Define o número mínimo de chamadas consideradas para avaliar se o circuito deve abrir.

Exemplo: ``java requestVolumeThreshold = 4``
Nesse caso, o Circuit Breaker avalia as últimas 4 chamadas.

### `failureRatio`

Define a proporção de falhas necessária para abrir o circuito.

Exemplo: `java failureRatio = 0.5`
Nesse caso, se 50% das chamadas falharem, o circuito poderá abrir.

### `delay`

Define quanto tempo o circuito ficará aberto antes de tentar chamadas novamente.

Exemplo: `java delay = 5000L`
Nesse caso, o circuito aguarda 5 segundos antes de ir para o estado `Half-Open`.

### `successThreshold`

Define quantas chamadas bem-sucedidas são necessárias no estado `Half-Open` para o circuito voltar ao estado `Closed`.

Exemplo: `java successThreshold = 2`
Nesse caso, são necessárias 2 chamadas bem-sucedidas para considerar o serviço recuperado.

## Timeout

Embora o foco principal seja fallback e circuit breaker, o projeto também utiliza timeout.

O **Timeout** define o tempo máximo que uma chamada pode levar antes de ser considerada falha.

Exemplo: `java @Timeout(3000L)`
Nesse caso, se a chamada demorar mais de 3 segundos, ela será interrompida e tratada como falha.
Esse recurso é importante porque evita que a aplicação fique esperando indefinidamente por um serviço externo.

## Health Check no Quarkus

O Quarkus permite expor endpoints de saúde da aplicação usando o **MicroProfile Health**.

Esses endpoints são muito úteis em ambientes como Docker, Kubernetes e plataformas de cloud, pois permitem verificar se a aplicação está viva e pronta para receber tráfego.

Os principais tipos de health check são:

- **Liveness**
- **Readiness**

## Liveness

O **Liveness Check** indica se a aplicação está viva.

Ele responde à pergunta:

> A aplicação ainda está em execução ou precisa ser reiniciada?

Se o liveness falhar, uma plataforma como Kubernetes pode reiniciar o container da aplicação.

### Para que serve?

O liveness serve para identificar problemas graves na aplicação, como:

- Deadlocks;
- Travamentos;
- Falhas internas irrecuperáveis;
- Aplicação sem capacidade de continuar executando.

### Endpoint

No Quarkus, normalmente o liveness pode ser acessado em: ` /q/health/live`


Também pode ser acessado junto com outros checks em: ` /q/health`


### Exemplo de resposta saudável
```json 
{ "status": "UP", "checks": }
```


## Readiness

O **Readiness Check** indica se a aplicação está pronta para receber tráfego.

Ele responde à pergunta:

> A aplicação está pronta para atender requisições agora?

Diferente do liveness, o readiness pode falhar sem que a aplicação precise ser reiniciada.

Por exemplo, a aplicação pode estar viva, mas não estar pronta porque depende de um serviço externo que está indisponível.

### Para que serve?

O readiness serve para informar se a aplicação pode receber requisições.

Ele pode considerar dependências como:

- Banco de dados;
- APIs externas;
- Filas;
- Serviços de autenticação;
- Cache;
- Serviços internos.

### Endpoint

No Quarkus, normalmente o readiness pode ser acessado em: `/q/health/ready`


Também pode ser acessado junto com outros checks em: `/q/health`


### Exemplo de resposta saudável
``` json
{ "status": "UP", "checks": }
```



### Exemplo de resposta não saudável
```json 
{ "status": "DOWN", "checks": }
```

## Diferença entre Liveness e Readiness

| Tipo | Pergunta principal | Quando usar | Ação esperada |
| --- | --- | --- | --- |
| Liveness | A aplicação está viva? | Para detectar travamentos ou falhas graves | Reiniciar a aplicação |
| Readiness | A aplicação está pronta? | Para verificar se pode receber tráfego | Remover temporariamente do balanceamento |
| Startup | A aplicação terminou de iniciar? | Para aplicações com inicialização lenta | Aguardar antes de aplicar liveness/readiness |

## Fluxo esperado da aplicação

1. A aplicação recebe uma requisição em `/time-app`;
2. Ela chama uma API externa de horário;
3. Se a chamada funcionar, retorna o horário obtido da API;
4. Se a chamada falhar, demorar demais ou o circuito estiver aberto, o fallback é executado;
5. O readiness pode indicar `DOWN` caso a aplicação dependa da API externa e ela não esteja respondendo corretamente;
6. O liveness continua indicando se a aplicação em si está viva.

## Endpoints úteis

### Endpoint da aplicação
`GET /time-app`


### Health geral
` GET /q/health`

### Liveness
` GET /q/health/live`


### Readiness
` GET /q/health/ready`


## Como executar em modo desenvolvimento

Execute a aplicação com:
```bash ./mvnw quarkus:dev```


A aplicação ficará disponível em: `text [http://localhost:8080](http://localhost:8080)`


## Como testar

Com a aplicação em execução, acesse:
bash curl [http://localhost:8080/time-app](http://localhost:8080/time-app)


Para verificar o health geral:

``bash curl [http://localhost:8080/q/health](http://localhost:8080/q/health)``

Para verificar liveness:

``bash curl [http://localhost:8080/q/health/live](http://localhost:8080/q/health/live)``


Para verificar readiness:

``bash curl [http://localhost:8080/q/health/ready](http://localhost:8080/q/health/ready)``

Para realizar chamadas em loop e ver o circuit breaker e o fault tolerance em ação, execute no bash:
```bash 
while true
do curl http://localhost:8080/time-app
sleep 2
done
```
Esse comando executa uma requisição HTTP GET para `http://localhost:8080/time-app` a cada 2 segundos, exibindo o resultado no terminal.

Para interromper o loop sem fechar o terminal, pressione: `Ctrl + C` no terminal.
