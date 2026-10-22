# Bookstore — E-shop Backend

[![CI](https://github.com/VasNera/bookstore/actions/workflows/ci.yml/badge.svg)](https://github.com/VasNera/bookstore/actions/workflows/ci.yml)

REST API for an online store that sells **books, music, toys and stationery**, built with Spring Boot: customers browse and filter the catalogue, keep a server-side cart, check out, track and cancel orders, write reviews and save favourites; an admin manages the catalogue and processes orders.

<!-- ![Swagger UI](docs/screenshots/swagger.png) -->

## Features

- **Catalogue with JPA inheritance**: an abstract `Product` with `Book`, `Music`, `Toy` and `Stationery` subtypes (`@Inheritance(JOINED)`), so cart items and order lines point to a single table. Reads are unified (`GET /products`), writes are per type (`POST /admin/books`, …).
- **Category tree**: self-referencing categories; products belong only to leaf categories; deleting a category that still has subcategories or products is refused, and moving a category under its own descendant is rejected (it would break the recursive tree).
- **Dynamic filtering and pagination** with Spring Data Specifications: search, category, price range, featured, on sale and product type, freely combined.
- **Server-side cart**: one per user, totals calculated on the server using the effective price (discount only when it is really lower than the price), stock checked when adding and updating.
- **Transactional checkout**: decreases the stock, empties the cart, stores **price and title snapshots** on every order line, generates the order number (`ORD-2026-000001`), validates invoice data (business name, tax number, street, profession) and supports receipt or invoice.
- **No overselling under concurrency**: `@Version` optimistic locking on products — a concurrent modification is answered with `409 Conflict`.
- **Order lifecycle**: `PENDING → CONFIRMED → SHIPPED → DELIVERED`, or `CANCELLED` (not allowed once shipped); invalid transitions are rejected. The **payment status** is a separate state machine (`PENDING → PAID / FAILED`, `PAID → REFUNDED`). Cancelling restores the stock and refunds a paid order.
- **Ownership enforced**: a customer can only see and cancel their own orders; an admin can see every order and filter them by status.
- **Reviews**: one per user per product, the public author name is only the first name and the initial of the last name (never the username), the average rating is calculated on demand.
- **Favourites**: idempotent add and remove, deleted products are hidden.
- **Stateless JWT security** with role **and capability** based authorization (`@PreAuthorize("hasAuthority('MANAGE_PRODUCTS')")` and 12 more capabilities stored in the database). Changing a role's capabilities takes effect immediately, with no new login.
- **Safe by construction**: the role, the review author, the order items, the total and the status are never taken from the client (no mass assignment); wrong password and unknown user give the **identical** `401`, so users cannot be enumerated.
- **Consistent error handling**: one JSON error shape, a validation error carries a map of the invalid fields; messages in Greek.
- Soft delete on products and users (orders keep referring to products that were removed), audit columns, `BigDecimal` money values.
- **OpenAPI / Swagger UI** with every endpoint documented (tags, summaries, response codes and schemas) and a Bearer authorization button.

## Tech stack

| Concern | Choice |
|---|---|
| Language / runtime | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation), Hibernate 7 |
| Database | MySQL 8, schema written by hand in **Flyway** migrations (`ddl-auto=validate`) |
| Auth | Spring Security + JWT (jjwt 0.13), BCrypt (strength 12) |
| API docs | springdoc-openapi 3 (Swagger UI) |
| Tests | JUnit 5, Mockito, AssertJ, Spring MockMvc, **Testcontainers** (MySQL 8.4) |
| CI | GitHub Actions |
| Build | Maven |

## API overview

All endpoints are prefixed with `/api/v1` (42 in total). The list is interactive in Swagger UI.

| Area | Endpoints | Access |
|---|---|---|
| Authentication | `POST /auth/register`, `POST /auth/login` | public |
| Profile | `GET /users/me`, `PUT /users/me`, `PUT /users/me/password` | logged in |
| Categories | `GET /categories` (nested tree) | public |
| Products | `GET /products` (filters + paging), `GET /products/{id}` | public |
| Reviews | `GET /products/{id}/reviews`, `GET /products/{id}/reviews/average`, `GET /reviews/latest` | public |
| Reviews | `POST /products/{id}/reviews`, `DELETE /reviews/{id}` | `CREATE_REVIEW`, `MANAGE_OWN_REVIEWS` |
| Cart | `GET /cart`, `POST /cart/items`, `PATCH` / `DELETE /cart/items/{productId}`, `DELETE /cart` | `MANAGE_OWN_CART` |
| Orders | `POST /orders` (checkout), `GET /orders`, `GET /orders/{orderNumber}`, `PATCH /orders/{orderNumber}/cancel` | `CREATE_ORDER`, `VIEW_OWN_ORDERS`, `CANCEL_OWN_ORDERS` |
| Favourites | `GET /favourites`, `POST` / `DELETE /favourites/{productId}` | logged in |
| Admin — categories | `POST /admin/categories`, `PUT` / `DELETE /admin/categories/{id}` | `MANAGE_CATEGORIES` |
| Admin — products | `POST /admin/books`, `/music`, `/toys`, `/stationery`; `PUT` on each `/{id}`; `DELETE /admin/products/{id}`; `PATCH /admin/products/{id}/featured` | `MANAGE_PRODUCTS` |
| Admin — orders | `GET /admin/orders?status=`, `GET /admin/orders/{orderNumber}`, `PATCH .../status`, `PATCH .../payment-status` | `MANAGE_ORDERS` |

Interactive documentation: **`http://localhost:8080/swagger-ui.html`** — log in through `POST /auth/login`, press **Authorize**, paste the token (without `Bearer`) and try the secured endpoints.

## Design decisions

- **Optimistic locking here, pessimistic locking in my other project.** The stock is read by many customers but written only at checkout, so conflicts are rare and `@Version` avoids holding locks; a clash becomes a `409` the client can retry. In [Doctor App](https://github.com/VasNera/doctor-app) a time slot is a single contested row, so pessimistic locking fits better.
- **Never store what can be calculated.** Cart totals, availability (`IN_STOCK` / `LOW_STOCK` / `OUT_OF_STOCK`) and the average rating are computed on demand. Order lines, however, **do** store the price and title at the time of purchase, because those are historical facts.
- **`BigDecimal` is compared with `compareTo`, never `equals`** (`2.0` and `2.00` are the same price).
- **Business rules live in services and mappers**; entities are plain data holders.
- **Hand-written, named constraints in the migrations** (`pk_`, `uk_`, `fk_`, `ix_`, `chk_`), including `CHECK` constraints for ratings and quantities, so the database defends its own integrity.
- **One admin URL rule** (`/api/v1/admin/**`) plus a capability check on each endpoint: two layers, each easy to audit.
- **Checked exceptions with an explicit `rollbackFor`** on every write method, handled in one `@RestControllerAdvice`.

## Tests

Automated unit and integration tests, run on every push by GitHub Actions.

- **Unit tests (Mockito, no database)**: checkout (stock, price snapshots, totals, invoice validation, every failure path), cancellation and refunds, ownership checks, cart rules, reviews, favourites, price calculation and both status state machines.
- **Integration tests with Testcontainers**: the whole application runs against a real **MySQL 8.4 container**; Flyway builds the schema from scratch on every run, which proves the migrations and the entity mapping agree. Covered: migrations, authentication, authorization (401 / 403, capability removal taking effect at once), catalogue and filters, cart.

```bash
./mvnw test        # needs Docker for the integration tests
```

## Build & Deploy

There are two ways to run the application: **with Docker Compose**, which brings up the database and the API together with a single command, or **locally**, against your own MySQL installation.

### Option A — Docker Compose (recommended)

Requires only **Docker Desktop** — no Java or MySQL installation needed.

```bash
git clone https://github.com/VasNera/bookstore.git
cd bookstore
cp .env.example .env
docker compose up --build
```

`.env.example` ships with working development values, so the copy above is enough to start the application. Open `.env` only if you want your own passwords or a different port.

`docker compose up` performs the whole build and deployment:

1. **Builds the application image** from the [`Dockerfile`](Dockerfile) — a multi-stage build that compiles the project with the Maven wrapper on `eclipse-temurin:21-jdk` and copies only the resulting jar into a slim `eclipse-temurin:21-jre` image, running as a non-root user.
2. **Starts MySQL 8.4** with a named volume (`mysql-data`), so the database survives restarts, and waits for its healthcheck before starting the API.
3. **Runs the Flyway migrations** on the first start, creating the schema and seeding the roles and capabilities.
4. **Creates the admin account** (development profile) with the password from `ADMIN_PASSWORD`.

| Service | URL |
|---|---|
| REST API | `http://localhost:8080/api/v1` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| MySQL | `localhost:3308` (mapped from 3306 inside the container, to avoid clashing with a local MySQL) |

If port 8080 is already in use, set `APP_PORT` in `.env`.

```bash
docker compose down       # stop, keep the database
docker compose down -v    # stop and delete the database volume
```

### Option B — Run locally

Requires **Java 21** and **MySQL 8**. The Maven wrapper is included.

**1. Create the database:**

```sql
CREATE DATABASE bookstore CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'bookstoreUser'@'localhost' IDENTIFIED BY '<your password>';
GRANT ALL PRIVILEGES ON bookstore.* TO 'bookstoreUser'@'localhost';
```

**2. Provide the configuration.** Create `src/main/resources/application-secret.properties` (git-ignored, imported automatically):

```properties
MYSQL_PASSWORD=<the password from step 1>
JWT_SECRET_KEY=<Base64-encoded key, at least 256 bits>
admin.password=<password for the seeded admin account>
```

To generate a key:

```bash
openssl rand -base64 32
```

**3. Run:**

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Flyway creates the schema and seeds the roles and capabilities on the first start, and the development profile creates the admin account. The application is available on `http://localhost:8080`.

### Demo accounts

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `ADMIN_PASSWORD` from `.env` (Docker) or `admin.password` from your secret file (local) |
| Customer | register through `POST /auth/register` | your own |

### Configuration through environment variables

Configuration is fully externalised: every setting can be supplied as an environment variable, which is how the Docker image and the CI pipeline are configured and how the application would be deployed to a host such as Railway or Render.

| Variable | Default | Purpose |
|---|---|---|
| `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DB` | `localhost` / `3306` / `bookstore` | database location |
| `MYSQL_USER` | `bookstoreUser` | database user |
| `MYSQL_PASSWORD` | — | database password (**required**) |
| `JWT_SECRET_KEY` | — | Base64 JWT signing key (**required**) |
| `ADMIN_PASSWORD` | — | password of the seeded `admin` account (**required** in the development profile) |
| `SPRING_DATASOURCE_URL` | — | overrides the whole JDBC URL, if needed |

## Roadmap

- React frontend in its own repository
- Product image upload
- Real payment provider (payments are mocked today)
