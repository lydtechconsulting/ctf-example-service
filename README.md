# Component Test Framework Example Service

Spring Boot application that showcases the component-test-framework for component testing.

The framework is available on Maven Central.  View usage here:

https://github.com/lydtechconsulting/component-test-framework

This example application uses Kafka as the broker, Postgres as the database, and uses the Transactional Outbox pattern with Debezium (Kafka Connect) for Change Data Capture (CDC) to publish outbound events.  

The service calls out to a third party service via REST.  This is represented by a standalone wiremock in the component tests.

## Debezium (Kafka Connect) Connector

The Debezium Postgres source connector configuration is defined in `resources/connector/outbox-connector.json`.

It includes a Single Message Transform (SMT) that routes the outbox event to the value of the destination field in the outbox event database table.

## Component Tests

The component tests treat the application as a black box performing end-to-end testing and proving the deployment and configuration in a local environment.

Build Spring Boot application jar, with Java 17:
```
mvn clean install
```

Build Docker container:
```
docker build -t ct/ctf-example-service:latest .
```

Run tests:
```
mvn test -Pcomponent
```

Run tests leaving containers up:
```
mvn test -Pcomponent -Dcontainers.stayup
```

## Clean Up Commands

- Manual clean up (if left containers up):
```
docker rm -f $(docker ps -aq)
```
