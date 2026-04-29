# p4pa-send-notification

This application belong to the **outbound** tier of the **Piattaforma Unitaria** product.

See [PU Microservice Architecture](https://raw.githubusercontent.com/pagopa/p4pa-doc/refs/heads/main/reference/technical-docs/Architettura_microservizi.pdf) for more details.

See [p4pa-doc](https://github.com/pagopa/p4pa-doc) for further documentation.

## 🧱 Role

* To send notification through PagoPA's SEND service.

## 🌐 APIs
See [OpenAPI](openapi/generated.openapi.json), exposed through the following path:
* `/swagger-ui/index.html`

See [Postman collection](/postman/P4PA-Send-E2E.postman_collection.json) and [Postman Environment](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1094615081/Environment+collection+postman).

### 📌 Relevant APIs
* `POST /p4pasend/notification`: To create a notification and retrieve a URL (towards [p4pa-fileshare](https://github.com/pagopa/p4pa-fileshare)) where to upload the configured files;
* `GET /p4pasend/notification/{sendNotificationId}`: To get notification status.

### 📌 Common HTTP status returned:
* `401`: Invalid access token provided, thus a new login is required;
* `403`: Trying to access a not authorized resource.

## 🔎 Monitoring
See available actuator endpoints through the following path:
* `/actuator`

### 📌 Relevant endpoints
* Health (provide an accessToken to see details): `/actuator/health`
  * Liveness: `/actuator/health/liveness`
  * Readiness: `/actuator/health/readiness`
* Metrics: `/actuator/metrics`
  * Prometheus: `/actuator/prometheus`

Further endpoints are exposed through the JMX console.

## ✏️ Logging
See [log configured pattern](/src/main/resources/logback-spring.xml).

## 🔗 Dependencies

### 🗄️ Resources
* PostgreSQL (citizen)
* MongoDB
* Shared folder

### 🧩 Microservices
* [p4pa-debt-positions](https://github.com/pagopa/p4pa-debt-positions):
  * To retrieve a debt position given a notice number;
* [p4pa-organization](https://github.com/pagopa/p4pa-organization):
  * To obtain organizations' SEND api key;
* [p4pa-workflow-hub](https://github.com/pagopa/p4pa-workflow-hub):
  * To start SEND notification workflow;
* [p4pa-pdnd-services](https://github.com/pagopa/p4pa-pdnd-services):
  * To retrieve PDND access token.

## 🗃️ Entities handled
* `send_notification`
* `send_stream`

### 🌍 External
* SEND - PagoPA's service to send legal communications towards citizen:
  * [OpenAPI](openapi/send-api-external-b2b-pa-bundle.yaml): To obtain access token towards PDND exposed services;

## 🔧 Configuration

See [application.yml](src/main/resources/application.yml) for each configurable property.

### 📌 Relevant configurations

#### 🌐 Application Server
| ENV         | DESCRIPTION                       | DEFAULT |
|-------------|-----------------------------------|---------|
| SERVER_PORT | Application server listening port | 8080    |

#### ✏️ Logging
| ENV                                   | DESCRIPTION                                                                                                                                                                     | DEFAULT |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| LOG_LEVEL_ROOT                        | Base level                                                                                                                                                                      | INFO    |
| LOG_LEVEL_PAGOPA                      | Base level of custom classes                                                                                                                                                    | INFO    |
| LOG_LEVEL_SPRING                      | Level applied to Spring framework                                                                                                                                               | INFO    |
| LOG_LEVEL_SPRING_BOOT_AVAILABILITY    | To print availability events                                                                                                                                                    | DEBUG   |
| LOGGING_LEVEL_API_REQUEST_EXCEPTION   | Level applied to APIs exception                                                                                                                                                 | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG             | Level applied to [PerformanceLog](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1540096383/Logging#2.2.-Log-di-performance)                                               | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG_API_REQUEST | Level applied to [API Performance Log](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1540096383/Logging#2.2.2.1.-Log-di-perfomance-per-le-API)                            | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG_REST_INVOKE | Level applied to [REST invoke Performance Log](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1540096383/Logging#2.2.2.2.-Log-di-performance-per-i-servizi-REST-integrati) | INFO    |

#### 🔁 Integrations

##### 🗄️ Resources
| ENV                                              | DESCRIPTION                                                                           | DEFAULT                                                       |
|--------------------------------------------------|---------------------------------------------------------------------------------------|---------------------------------------------------------------|
| SHOW_SQL                                         | To print SQL statements                                                               | false                                                         |
| CITIZENDB_URL                                    | Citizen PostgreSQL connection string (to use in order to customize the entire string) | jdbc:postgresql://${CITIZENDB_HOST}:${CITIZENDB_PORT}/citizen |
| CITIZENDB_HOST                                   | Citizen PostgreSQL Host                                                               | localhost                                                     |
| CITIZENDB_PORT                                   | Citizen PostgreSQL port                                                               | 5432                                                          |
| CITIZENDB_NAME                                   | Citizen PostgreSQL Database name                                                      | payhub                                                        |
| CITIZENDB_USER                                   | Citizen PostgreSQL username                                                           |                                                               |
| CITIZENDB_PASSWORD                               | Citizen PostgreSQL password                                                           |                                                               |
| CITIZENDB_CONNECTION_IDLE_TIMEOUT_MILLISECONDS   | Citizen PostgreSQL connection idle timeout (milliseconds)                             | 600000                                                        |
| CITIZENDB_CONNECTION_TIMEOUT_MILLISECONDS        | Citizen PostgreSQL connection timeout (milliseconds)                                  | 30000                                                         |
| CITIZENDB_CONNECTION_KEEPALIVE_TIME_MILLISECONDS | Citizen PostgreSQL connection keepalive time (milliseconds)                           | 120000                                                        |
| CITIZENDB_CONNECTION_MAX_LIFETIME_MILLISECONDS   | Citizen PostgreSQL connection max lifetime (milliseconds)                             | 1800000                                                       |
| CITIZENDB_CONNECTION_MAX_POOL_SIZE               | Citizen PostgreSQL connection max pool size                                           | 10                                                            |
| CITIZENDB_CONNECTION_MIN_IDLE                    | Citizen PostgreSQL connection min idle                                                | 10                                                            |
| MONGODB_URI                                      | Mongo connection string                                                               | mongodb://localhost:27017                                     |
| MONGODB_DBNAME                                   | Mongo db name                                                                         | payhub                                                        |
| MONGODB_CONNECTIONPOOL_MAX_SIZE                  | Mongo connection pool max size                                                        | 100                                                           |
| MONGODB_CONNECTIONPOOL_MIN_SIZE                  | Mongo connection pool max size                                                        | 0                                                             |
| MONGODB_CONNECTIONPOOL_MAX_WAIT_MS               | Timeout milliseconds                                                                  | 120000                                                        |
| MONGODB_CONNECTIONPOOL_MAX_CONNECTION_LIFE_MS    | Connection lifetime (milliseconds)                                                    | 0                                                             |
| MONGODB_CONNECTIONPOOL_MAX_CONNECTION_IDLE_MS    | Connection idle lifetime (milliseconds)                                               | 120000                                                        |
| MONGODB_CONNECTIONPOOL_MAX_CONNECTING            | Max parallel creating connections                                                     | 2                                                             |
| SHARED_FOLDER_ROOT                               | Absolute path towards shared folder on file system                                    | /shared                                                       |

##### 📋 [Caching](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1542128077/Caching)
| ENV                           | DESCRIPTION                                | DEFAULT |
|-------------------------------|--------------------------------------------|---------|
| CACHE_PII_SIZE                | PII cache size                             | 1000    |
| CACHE_PII_MINUTES             | PII cache retention (minutes)              | 60      |
| CACHE_PDNDACCESSTOKEN_SIZE    | PDND accessToken cache size                | 100     |
| CACHE_PDNDACCESSTOKEN_MINUTES | PDND accessToken cache retention (minutes) | 10      |

##### 🔗 REST
| ENV                                               | DESCRIPTION                               | DEFAULT |
|---------------------------------------------------|-------------------------------------------|---------|
| DEFAULT_REST_CONNECTION_POOL_SIZE                 | Default connection pool size              | 10      |
| DEFAULT_REST_CONNECTION_POOL_SIZE_PER_ROUTE       | Default connection pool size per route    | 5       |
| DEFAULT_REST_CONNECTION_POOL_TIME_TO_LIVE_MINUTES | Default connection pool TTL (minutes)     | 10      |
| DEFAULT_REST_TIMEOUT_CONNECT_MILLIS               | Default connection timeout (milliseconds) | 120000  |
| DEFAULT_REST_TIMEOUT_READ_MILLIS                  | Default read timeout (milliseconds)       | 120000  |

##### 🧩 Microservices
| ENV                                 | DESCRIPTION                                     | DEFAULT |
|-------------------------------------|-------------------------------------------------|---------|
| ORGANIZATION_BASE_URL               | Organization microservice URL                   |         |
| ORGANIZATION_MAX_ATTEMPTS           | Organization API max attempts                   | 3       |
| ORGANIZATION_WAIT_TIME_MILLIS       | Organization retry waiting time (milliseconds)  | 500     |
| ORGANIZATION_PRINT_BODY_WHEN_ERROR  | To print body when an error occurs              | true    |
| WORKFLOW_HUB_BASE_URL               | WorkflowHub microservice URL                    |         |
| WORKFLOW_HUB_MAX_ATTEMPTS           | WorkflowHub API max attempts                    | 3       |
| WORKFLOW_HUB_WAIT_TIME_MILLIS       | WorkflowHub retry waiting time (milliseconds)   | 500     |
| WORKFLOW_HUB_PRINT_BODY_WHEN_ERROR  | To print body when an error occurs              | true    |
| DEBT_POSITION_BASE_URL              | DebtPositions microservice URL                  |         |
| DEBT_POSITION_MAX_ATTEMPTS          | DebtPositions API max attempts                  | 3       |
| DEBT_POSITION_WAIT_TIME_MILLIS      | DebtPositions retry waiting time (milliseconds) | 500     |
| DEBT_POSITION_PRINT_BODY_WHEN_ERROR | To print body when an error occurs              | true    |
| PDND_BASE_URL                       | PdndServices microservice URL                   |         |
| PDND_MAX_ATTEMPTS                   | PdndServices API max attempts                   | 3       |
| PDND_WAIT_TIME_MILLIS               | PdndServices retry waiting time (milliseconds)  | 500     |
| PDND_PRINT_BODY_WHEN_ERROR          | To print body when an error occurs              | true    |

##### 🌍 External services
| ENV                              | DESCRIPTION                                                                                                                 | DEFAULT                                        |
|----------------------------------|-----------------------------------------------------------------------------------------------------------------------------|------------------------------------------------|
| SEND_BASE_URL                    | PDND service URL                                                                                                            |                                                |
| SEND_MAX_ATTEMPTS                | PDND API max attempts                                                                                                       | 3                                              |
| SEND_WAIT_TIME_MILLIS            | PDND retry waiting time (milliseconds)                                                                                      | 500                                            |
| SEND_PRINT_BODY_WHEN_ERROR       | To print body when an error occurs                                                                                          | true                                           |

#### 💼 Business logic
| ENV                       | DESCRIPTION                                                                                | DEFAULT |
|---------------------------|--------------------------------------------------------------------------------------------|---------|
| FILESHARE_PUBLIC_BASE_URL | Public base URL towards [p4pa-fileshare](https://github.com/pagopa/p4pa-fileshare) service |         |

#### 🔑 keys
| ENV                          | DESCRIPTION                                               | DEFAULT |
|------------------------------|-----------------------------------------------------------|---------|
| JWT_TOKEN_PUBLIC_KEY         | p4pa-auth JWT public key                                  |         |
| DATA_CIPHER_HASH_PEPPER      | Base64 encoded key (256 bit) used to calculate hash       |         |
| DATA_CIPHER_ENCRYPT_PASSWORD | Base64 encoded key (256 bit) used to encrypt data         |         |
| FILE_ENCRYPT_PASSWORD        | Base64 encoded key (256 bit) used to encrypt/decrypt file |         |

## 🛠️ Getting Started

### 📝 Prerequisites

Ensure the following tools are installed on your machine:

1. **Java 21+**
2. **Gradle** (or use the Gradle wrapper included in the repository)
3. **Docker** (to build and run on an isolated environment, optional)

### 🔐 Write Locks

```sh
./gradlew dependencies --write-locks
```

### ⚙️ Build

```sh
./gradlew clean build
```

### 🧪 Test

#### 📌 JUnit
```sh
./gradlew test
```

### 🚀 Run local

```sh
./gradlew bootRun
```

### 🐳 Build & run through Docker
```sh
docker build -t <APP_NAME> .
docker run --env-file <ENV_FILE> <APP_NAME>
```

### ⚖️ Generate dependencies licenses
```sh
./gradlew generateLicenseReport
```
