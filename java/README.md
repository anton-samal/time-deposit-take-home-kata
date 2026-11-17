# XA Bank Time Deposit – Java solution

Spring Boot 4.1 · Java 25 · Hexagonal architecture · PostgreSQL + Flyway · OpenAPI (contract-first) · Testcontainers · ArchUnit

## How to run

**Prerequisites:** Java 25, Maven 3.9+, Docker (running).

```bash
cd java
mvn spring-boot:run
```

PostgreSQL starts automatically from `compose.yaml` (Spring Boot Docker Compose support). Flyway creates the schema and
loads demo data: one deposit per plan type on each side of its interest boundary, plus some withdrawals.

### Trigger the endpoints with Swagger UI

1. Open **http://localhost:8080/swagger-ui.html**.
2. **GET `/time-deposits`** → *Try it out* → *Execute*: lists the 6 demo deposits with their withdrawals.
3. **POST `/time-deposits/balances`** → *Try it out* → *Execute*: returns `204 No Content`.
4. Run the **GET** again. Balances that earn interest have grown by one month, for example:

| id | plan | days | before | after one POST | why |
|---|---|---|---|---|---|
| 1 | basic | 30 | 10000.00 | 10000.00 | no interest in the first 30 days |
| 2 | basic | 31 | 10000.00 | 10008.33 | 1% / 12 |
| 3 | student | 365 | 20000.00 | 20050.00 | 3% / 12, still within the first year |
| 4 | student | 366 | 20000.00 | 20000.00 | no interest after one year |
| 5 | premium | 45 | 50000.00 | 50000.00 | interest starts after 45 days |
| 6 | premium | 46 | 50000.00 | 50208.33 | 5% / 12 |

The contract is in [`src/main/resources/openapi/time-deposits.yaml`](src/main/resources/openapi/time-deposits.yaml);
the raw OpenAPI document is served at http://localhost:8080/v3/api-docs.

Without Swagger:
```bash
curl http://localhost:8080/time-deposits
curl -X POST http://localhost:8080/time-deposits/balances
```

### Run the tests

```bash
cd java
mvn verify          # Docker must be running (Testcontainers)
```
The coverage report is written to `target/site/jacoco/index.html`.

## What I found in the existing code

- The interest logic in `TimeDepositCalculator.updateBalance` matched the requirements, but it had **no real test**
  (the only one asserted `1 == 1`).
- The rules were hard-coded in nested `if`/`else` on plan-type strings, so every new plan meant editing the method
  (violates the Open/Closed principle). There were also magic numbers and unclear names (`xs`, `a2d`).
- Rounding uses `new BigDecimal(double)`. It is kept on purpose: `BigDecimal.valueOf` would round some half-cent
  amounts differently, and the README says the current behaviour is correct.
- The API, the database and withdrawals were missing.

## How I approached it (one commit per step)

1. **Characterization tests** for the existing calculator, before touching it: boundaries as triples
   (29/30/31 days for all plans, 44/45/46 premium, 364/365/366 student), rounding, exact plan-type matching, several deposits per call.
2. **Coverage review** with JaCoCo: 100% line and branch coverage of the calculator before refactoring.
3. Build upgrade to Spring Boot 4.1.2 / Java 25.
4. **Refactoring** to Strategy + Template Method (below). The same tests stay green, proving the behaviour is unchanged.
5. Application layer, persistence adapter, REST adapter, each with its own tests.
6. Architecture rules as ArchUnit tests, then a final review (dead code, coverage, dependencies).

## Design

```
org.ikigaidigital
├─ TimeDeposit, TimeDepositCalculator     domain; unchanged public API (shared classes stay in their package)
├─ domain/interest/                       InterestPolicy (Strategy), AnnualRateInterestPolicy (Template Method),
│                                         BasicInterestPolicy, StudentInterestPolicy, PremiumInterestPolicy
├─ domain/Withdrawal                      record
├─ application/port/in/                   UpdateAllBalancesUseCase, GetAllTimeDepositsUseCase, TimeDepositView
├─ application/port/out/                  TimeDepositRepositoryPort, WithdrawalRepositoryPort
├─ application/TimeDepositService         implements both use cases
├─ adapter/in/web/                        controller implementing the interface generated from the OpenAPI contract
├─ adapter/out/persistence/               JPA entities, Spring Data repositories, port implementations
└─ config/                                Spring wiring of the framework-free core
```

**Why these decisions:**
- **Strategy for interest rules:** a new plan is one new class, and the calculator doesn't change. Each policy owns its
  eligibility rule; the shared formula (`balance × rate / 12`) and the 30-day constant live in the template base class.
- **`TimeDepositCalculator` keeps its no-arg constructor and `updateBalance` signature**, so no caller breaks. A second
  constructor accepts custom policies, as shown in a test.
- **Hexagonal architecture:** the domain and application layers have no Spring, JPA or HTTP code. They depend on ports;
  adapters implement them. `ArchitectureTest` fails the build if a dependency points the wrong way.
- **Contract-first API:** the controller implements the generated `TimeDepositsApi`, so it can't drift from the contract.
  Generated models are mapped at the adapter boundary and never reach the core.
- **Exactly two endpoints.** `POST /time-deposits/balances` (not `PUT`), because each call adds another month of interest,
  so it is not idempotent. It returns `204`; the result is read with the `GET`.
- **Money:** the database and the API use 2-decimal values (`NUMERIC(19,2)`, JSON numbers like `10008.33`); `TimeDeposit`
  keeps `Double` because it must not change. The conversion happens only in the adapters.
- **No lost updates:** the balance update runs in one transaction and locks the rows with `SELECT … FOR UPDATE`, so a
  second, overlapping update waits instead of overwriting the first one. Without it, two `POST`s at the same moment
  could read the same balances and one month of interest would be lost (reproduced by `ConcurrentBalanceUpdateTest`:
  8 parallel updates gave 1 month of interest before the fix, 8 after). The transaction is started in `config`
  with a `TransactionTemplate`, so the core needs no `@Transactional`. Found during the final review and fixed in its own
  commit: the change touched only one out-port method, the persistence adapter and the config; the domain and the
  interest rules stayed untouched.
- **The `GET` runs two queries** (deposits + withdrawals, grouped in memory) instead of one withdrawal query per deposit.
- **Tests:** in-memory fakes instead of mocks for the service; real PostgreSQL (Testcontainers) for persistence and API tests.

## Assumptions

- **One year = 365 days** for the student plan (leap years ignored), matching the existing `days < 366` check.
- **Withdrawals don't affect interest:** the existing logic ignores them and the README doesn't say otherwise.
- **The balance update doesn't change `days`.** It applies exactly one month of interest per call; ageing deposits is outside this task.
- **Table and column names are snake_case** (`time_deposits.plan_type`, `withdrawals.time_deposit_id`), the PostgreSQL
  convention, mapped from the README's camelCase names.
- **Withdrawals are loaded by a Flyway seed migration**, because no endpoint may create them. Tests don't use the seed
  data; each test inserts its own.
- **Invalid input is out of scope.** Unknown plan types earn no interest (as before). A `null` plan type now also earns
  no interest instead of throwing `NullPointerException`.

## Known limitations / next steps

- **Large data sets:** the update loads all deposits at once. Next step: process in pages or batches.
- **`Double` in the domain** is kept because `TimeDeposit` must not change; a `Money` value object would be the long-term fix.
- **Contract tests:** once the API has real consumers, I'd add consumer-driven contract tests (Spring Cloud Contract or Pact),
  so a change to the API can't silently break a client. Today the contract-first setup already catches provider-side drift
  at compile time (the controller implements the interface generated from the OpenAPI file). A cheap first step would be to
  validate the end-to-end test responses against the OpenAPI schema.

## AI-assisted development

See [`../docs/AI_ASSISTED_DEVELOPMENT.md`](../docs/AI_ASSISTED_DEVELOPMENT.md) and the agent rules in [`../CLAUDE.md`](../CLAUDE.md).
