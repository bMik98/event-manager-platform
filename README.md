# Event Manager Platform

## Сборка и запуск

Сборка всего проекта:

```bash
mvn clean verify
```

### Конфигурация секретов

Секреты не хранятся в репозитории. Перед запуском нужно задать переменные окружения:

| Переменная               | Назначение                                  | Обязательна |
|--------------------------|---------------------------------------------|-------------|
| `JWT_SECRET`             | base64-ключ (256+ бит) для подписи JWT      | да          |
| `DEFAULT_ADMIN_PASSWORD` | пароль создаваемого по умолчанию админа     | да          |
| `JWT_EXPIRATION_MILLIS`  | срок жизни токена (по умолчанию 24 часа)    | нет         |
| `DEFAULT_ADMIN_LOGIN`    | логин админа (по умолчанию `admin`)         | нет         |

Сгенерировать ключ:

```bash
openssl rand -base64 32
```

Без `JWT_SECRET` и `DEFAULT_ADMIN_PASSWORD` приложение намеренно не стартует
(fail-fast), чтобы исключить запуск с известным ключом.

Для локальной разработки удобнее положить значения в
`event-manager/src/main/resources/application-local.yaml` (файл в `.gitignore`)
и запускать с профилем `local`.

Запуск `event-manager`:

```bash
# вариант 1: переменные окружения
JWT_SECRET=$(openssl rand -base64 32) DEFAULT_ADMIN_PASSWORD='ChangeMe123!' \
  mvn -pl event-manager spring-boot:run

# вариант 2: профиль local с application-local.yaml
mvn -pl event-manager spring-boot:run -Dspring-boot.run.profiles=local
```

Запуск тестов только модуля `event-manager`:

```bash
mvn -pl event-manager test
```
