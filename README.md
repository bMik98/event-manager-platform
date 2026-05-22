# Event Manager Platform

Учебный стартовый репозиторий для выпускного проекта по Java Backend.

Внутри уже подготовлены базовая Maven-структура, стартовая конфигурация Spring Boot,
`Docker Compose` для инфраструктуры и OpenAPI-контракты. Это позволяет быстрее
перейти к бизнес-логике, API и архитектуре сервиса.

## Что есть в репозитории

- `event-manager/` — стартовый модуль сервиса на Spring Boot.
- `infra/` — compose-файлы для локального подъема PostgreSQL, Kafka и Redis.
- `docs/openapi/event-manager-openapi.yaml` — контракт API сервиса `event-manager`.
- `docs/openapi/event-notificator-openapi.yaml` — контракт API сервиса `event-notificator`.

## Стек проекта

- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring Security + JWT
- Liquibase
- PostgreSQL
- Kafka
- Redis
- Maven
- Docker Compose
- OpenAPI / Swagger

## Сборка и запуск

Сборка всего проекта:

```bash
mvn clean verify
```

Запуск `event-manager`:

```bash
mvn -pl event-manager spring-boot:run
```

Запуск тестов только модуля `event-manager`:

```bash
mvn -pl event-manager test
```
