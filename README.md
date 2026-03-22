# 💰 Smart Expense Tracker API

> RESTful бекенд-застосунок для ведення особистих фінансів на базі Spring Boot 4

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![CI](https://github.com/DenisKoriavets/smart-expense-tracker/actions/workflows/ci.yaml/badge.svg)](https://github.com/DenisKoriavets/smart-expense-tracker/actions)

---

## 📖 Про проєкт

**Smart Expense Tracker API** — це повноцінний RESTful бекенд для керування особистими фінансами. Застосунок дозволяє користувачам реєструватися, вести кілька рахунків у різних валютах, записувати доходи та витрати, встановлювати бюджети на категорії й отримувати детальну аналітику.

---

## ✨ Функціональність

### 🔐 Автентифікація та авторизація
- Реєстрація та вхід користувача
- Stateless-сесії на базі **JWT Access + Refresh токенів**
- Оновлення токена через `/api/auth/refresh`
- Logout з анулюванням Refresh Token
- Рольова авторизація: `USER` та `ADMIN`

### 💳 Рахунки (Accounts)
- Створення декількох рахунків на одного користувача
- Типи рахунків: `CASH`, `CARD`, `SAVINGS`, `INVESTMENT`
- Підтримка валют: `UAH`, `USD`, `EUR`
- Баланс рахунку обчислюється автоматично на основі транзакцій

### 🔄 Транзакції (Transactions)
- Запис доходів (`INCOME`) та витрат (`EXPENSE`)
- Прив'язка транзакції до рахунку та категорії
- Фільтрація за типом, категорією та часовим діапазоном
- Пагінований список транзакцій
- При видаленні транзакції баланс рахунку перераховується автоматично

### 🗂️ Категорії (Categories)
- Власні категорії доходів та витрат для кожного користувача
- Встановлення **місячного бюджету** на категорію
- Іконки категорій (iconCode)

### 📊 Аналітика (Analytics)
- **Загальна статистика** — сума доходів, витрат та загальний баланс за обраний період
- **Аналітика за категоріями** — розбивка витрат/доходів по категоріях з відсотками
- **Місячна статистика** — агреговані доходи та витрати згруповані помісячно
- **Статус бюджетів** — порівняння лімітів категорій з поточними витратами

### 🛡️ Адмін-панель
- Перегляд та управління користувачами (тільки для ролі `ADMIN`)

---

## 🛠️ Технологічний стек

| Категорія | Технологія |
|---|---|
| Мова | Java 21 |
| Фреймворк | Spring Boot 4.0.3 |
| Безпека | Spring Security + JWT (jjwt 0.11.5) |
| ORM | Spring Data JPA / Hibernate |
| БД (prod) | PostgreSQL 15 |
| БД (test) | H2 in-memory |
| Міграції | Liquibase |
| Валідація | Jakarta Bean Validation |
| Маппінг | MapStruct 1.6.3 |
| Кешування | Spring Cache + Caffeine |
| Документація | SpringDoc OpenAPI 3 (Swagger UI) |
| Тестування | JUnit 5 + Mockito + Spring Security Test |
| Збірка | Gradle |
| Контейнеризація | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## 🏗️ Архітектура

Проєкт побудовано за класичною **Layered Architecture**:

```
Controller  →  Service  →  Repository  →  Database
    ↕              ↕
  DTO ↔ Entity (MapStruct)
```

### Структура пакетів

```
src/main/java/com/github/deniskoriavets/smartexpensetracker/
├── config/             # Конфігурації (Cache, OpenAPI)
├── controller/         # REST-контролери
│   ├── AuthController
│   ├── AccountController
│   ├── TransactionController
│   ├── CategoryController
│   ├── AnalyticsController
│   └── AdminController
├── dto/                # Data Transfer Objects
│   ├── account/
│   ├── analytics/
│   ├── auth/
│   ├── category/
│   ├── transaction/
│   └── user/
├── entity/             # JPA-сутності
│   ├── enums/          # AccountType, CategoryType, Currency, Role, TransactionType
│   ├── Account
│   ├── Category
│   ├── Transaction
│   ├── User
│   └── RefreshToken
├── exception/          # Глобальна обробка помилок
├── mapper/             # MapStruct-маппери
├── repository/         # Spring Data JPA репозиторії
├── security/           # JWT-фільтр, конфігурація безпеки
└── service/            # Бізнес-логіка
```

---

## 🚀 Запуск проєкту

### Передумови

- [Docker](https://docs.docker.com/get-docker/) та [Docker Compose](https://docs.docker.com/compose/)
- [JDK 21](https://adoptium.net/) (для локального запуску без Docker)

### 1. Клонування репозиторію

```bash
git clone https://github.com/DenisKoriavets/smart-expense-tracker.git
cd smart-expense-tracker
```

### 2. Налаштування змінних середовища

Створіть файл `.env` у корені проєкту:

```env
JWT_SECRET_KEY=your-very-secret-key-at-least-256-bits-long
```

> ⚠️ Ніколи не комітьте `.env` у репозиторій. Він вже є у `.gitignore`.

### 3. Запуск через Docker Compose

```bash
# Зібрати Docker-образ застосунку
docker build -t smart-expense-tracker:latest .

# Запустити застосунок та базу даних
docker compose up -d
```

Застосунок буде доступний за адресою: **http://localhost:8080**

### 4. Локальний запуск (без Docker)

Переконайтесь, що PostgreSQL запущено та налаштовано, потім:

```bash
./gradlew bootRun
```

---

## 📋 API Endpoints

### 🔐 Auth — `/api/auth`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `POST` | `/register` | Реєстрація нового користувача | ❌ |
| `POST` | `/login` | Вхід та отримання токенів | ❌ |
| `POST` | `/refresh` | Оновлення Access Token | ❌ |
| `POST` | `/logout` | Вихід із системи | ❌ |

### 💳 Accounts — `/api/accounts`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `POST` | `/` | Створити рахунок | ✅ |
| `GET` | `/` | Отримати всі рахунки користувача | ✅ |
| `GET` | `/{id}` | Отримати рахунок за ID | ✅ |
| `PUT` | `/{id}` | Оновити рахунок | ✅ |
| `DELETE` | `/{id}` | Видалити рахунок | ✅ |

### 🔄 Transactions — `/api/transactions`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `POST` | `/` | Створити транзакцію | ✅ |
| `GET` | `/account/{accountId}` | Транзакції рахунку (з фільтрами та пагінацією) | ✅ |
| `GET` | `/{id}` | Отримати транзакцію за ID | ✅ |
| `PUT` | `/{id}` | Оновити транзакцію | ✅ |
| `DELETE` | `/{id}` | Видалити транзакцію | ✅ |

**Query-параметри для `GET /account/{accountId}`:**

```
type        — INCOME | EXPENSE
categoryId  — UUID категорії
from        — дата від (ISO 8601, напр. 2025-01-01T00:00:00)
to          — дата до (ISO 8601)
page, size  — пагінація (Spring Pageable)
```

### 🗂️ Categories — `/api/categories`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `POST` | `/` | Створити категорію | ✅ |
| `GET` | `/` | Отримати всі категорії | ✅ |
| `GET` | `/{id}` | Отримати категорію за ID | ✅ |
| `PUT` | `/{id}` | Оновити категорію | ✅ |
| `DELETE` | `/{id}` | Видалити категорію | ✅ |

### 📊 Analytics — `/api/analytics`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `GET` | `/summary` | Загальна статистика за період | ✅ |
| `GET` | `/categories` | Аналітика за категоріями | ✅ |
| `GET` | `/monthly` | Місячна статистика | ✅ |
| `GET` | `/budgets` | Статус бюджетів поточного місяця | ✅ |

### 🛡️ Admin — `/api/admin`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `GET` | `/users` | Список усіх користувачів | ✅ ADMIN |
| `PUT` | `/users/{id}/deactivate` | Деактивувати користувача | ✅ ADMIN |

---

## 📚 Swagger UI

Після запуску застосунку інтерактивна документація API доступна за адресою:

```
http://localhost:8080/swagger-ui.html
```

Специфікація OpenAPI у JSON:

```
http://localhost:8080/api-docs
```

---

## 🧪 Тестування

Проєкт містить юніт- та інтеграційні тести для всіх шарів застосунку.

```bash
# Запустити всі тести
./gradlew test

# Запустити тести з детальним виводом
./gradlew test --info
```

### Покриття тестами

| Шар | Тести |
|---|---|
| Controllers | `AccountControllerTest`, `AuthControllerTest`, `CategoryControllerTest`, `TransactionControllerTest`, `AnalyticsControllerTest` |
| Services | `AccountServiceTest`, `CategoryServiceTest`, `TransactionServiceTest`, `AnalyticsServiceTest` |
| Repositories | `AccountRepositoryTest`, `CategoryRepositoryTest`, `TransactionRepositoryTest`, `UserRepositoryTest` |

> Тести використовують **H2 in-memory** базу даних через профіль `test`, що дозволяє запускати їх без PostgreSQL.

---

## 🗄️ База даних

Схема БД керується через **Liquibase** і версіонується у вигляді міграцій:

```
src/main/resources/db/changelog/migrations/
├── 001-create-users-table.xml
├── 002-create-accounts-table.xml
├── 003-create-categories-table.xml
├── 004-create-transactions-table.xml
└── 005-create-refresh-tokens-table.xml
```

Міграції застосовуються автоматично при старті застосунку.

---

## ⚙️ Конфігурація

Застосунок підтримує профілі Spring:

| Профіль | Файл | Опис |
|---|---|---|
| `dev` (default) | `application-dev.yaml` | Локальна розробка, PostgreSQL |
| `test` | `application-test.yaml` | Тести, H2 in-memory |

Зміна активного профілю:

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

---

## 🔄 CI/CD

GitHub Actions автоматично запускає pipeline при кожному push або pull request у гілку `master`:

1. **Checkout** — клонування репозиторію
2. **Setup JDK 21** (Temurin distribution)
3. **Gradle Build** — збірка та запуск тестів (`./gradlew clean build`)
4. **Docker Build** — побудова Docker-образу

Конфігурація: `.github/workflows/ci.yaml`

---

## 🔒 Безпека

- Паролі зберігаються у хешованому вигляді (BCrypt)
- Stateless-автентифікація через **JWT Access Token** (короткоживучий)
- **Refresh Token** зберігається у БД і може бути анульований при logout
- Всі ендпоінти (крім `auth/*` та Swagger) захищені JWT-фільтром
- Адмін-ендпоінти доступні тільки для ролі `ROLE_ADMIN`
- Кешування (Caffeine) з TTL 5 хвилин для часто запитуваних даних

---

## 👤 Автор

**Denis Koriavets**

[![GitHub](https://img.shields.io/badge/GitHub-DenisKoriavets-181717?logo=github)](https://github.com/DenisKoriavets)
