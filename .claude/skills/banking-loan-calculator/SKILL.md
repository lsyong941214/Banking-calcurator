---
name: banking-loan-calculator
description: Reference design for the "금융개발" (financial development) work in this project — a Java/Spring Boot MSA loan calculation engine backed by PostgreSQL, plus its screens. Consult this whenever working in this repo on anything related to loans, interest/repayment calculation, loan-engine-service, loan-schedule-service, the loan_contract/repayment_schedule schema, the git commit/push workflow, or any screen/UI work (이자계산, 상환스케줄 보기, or new screens). Always check this skill before writing new Java classes, DB migrations, docker-compose changes, or HTML/CSS for a screen here, even if the request only mentions one small piece (e.g. "add a new repayment type", "fix the interest rounding", or "add a field to the screen") — it defines the conventions and design system the whole codebase must stay consistent with.
---

# 금융개발 — Loan Calculation MSA

This is the standing design reference for the loan/banking calculator built in this
project. Treat it as the source of truth for architecture, schema, and financial
correctness rules — don't improvise a different shape without flagging the deviation
to the user first.

## Project & git workflow

- Local path: `~/Desktop/Valueup2026/int_calc`
- GitHub remote (SSH): `git@github.com:lsyong941214/Banking-calcurator.git`
- **Rule**: all 금융개발 work in this repo gets committed to this remote. Make small,
  logical commits as pieces land (e.g. "loan-engine: add equal-principal calculation",
  not one giant commit at the end). Push after committing so the remote stays current.
  Still confirm before the *first* push of a session, and before any force-push or
  history rewrite — those remain irreversible actions that need explicit sign-off.

## Architecture

Two services, cleanly separated by responsibility:

```
       [Client / Postman]
               │
               ▼
   [Loan Schedule Service]  <--->  [PostgreSQL DB]
               │  (internal API call)
               ▼
   [Loan Engine Service]   (pure math, stateless)
```

- **loan-engine-service** (port 8081): stateless REST API, pure repayment-method math.
  No DB access, no persistence. Given principal/rate/term/method, returns a schedule.
- **loan-schedule-service** (port 8082): owns persistence. Accepts loan applications,
  calls loan-engine-service (OpenFeign or WebClient) to get the schedule, then saves
  contract + schedule rows in one `@Transactional` write.
- **PostgreSQL**: chosen specifically for `NUMERIC` precision and transactions —
  required for money, never optional.

Recommended module layout:

```
loan-msa-project/
├── docker-compose.yml
├── loan-engine-service/        # port 8081
│   └── src/main/java/com/example/engine/
│       ├── controller/         # POST /api/v1/calculate
│       └── service/            # repayment-method algorithms
└── loan-schedule-service/      # port 8082
    └── src/main/java/com/example/schedule/
        ├── controller/         # POST /api/v1/loans
        ├── client/             # OpenFeign / WebClient → engine service
        ├── domain/             # LoanContract, RepaymentSchedule JPA entities
        └── repository/         # Spring Data JPA repositories
```

## Database schema

Full DDL: [references/schema.sql](references/schema.sql). Two tables:

- `loan_contract` — one row per loan (principal, annual_rate, term_months,
  repayment_type, start_date).
- `repayment_schedule` — one row per installment, FK to `loan_contract`.

**Every money or rate column is `NUMERIC`, never `FLOAT`/`DOUBLE`.** This is
non-negotiable for financial data — floating point drift compounds across months
and produces balances that don't reconcile to zero at maturity.

`repayment_type` is one of `EQUAL_PRINCIPAL_AND_INTEREST` (원리금균등),
`EQUAL_PRINCIPAL` (원금균등), or `BULK` (만기일시) — the engine must support all three
with the same rigor, not just the first one.

Local DB comes up via `docker-compose up -d` using
[references/docker-compose.yml](references/docker-compose.yml) (postgres:15-alpine,
db `loandb`, mounts `init.sql` from the schema above for auto-init).

## Java calculation rules

Reference implementation: [references/LoanCalculator.java](references/LoanCalculator.java).
It covers all three repayment methods — use it as the starting point for
`loan-engine-service`'s calculation logic, not just the equal-principal-and-interest
case shown in early drafts.

Non-negotiable rules, because this is money:

1. **`BigDecimal` only.** Never `double`/`float` for principal, rate, interest, or
   any derived amount — binary floating point cannot represent decimal fractions
   like 0.1 exactly, and the error compounds over dozens of installments.
2. **`RoundingMode.HALF_UP`** consistently for interest/principal rounding, applied
   at each step (not just at the end) so intermediate values match what a real
   statement would show.
3. **Monthly rate = annual rate / 12**, divided with enough scale (10 digits) before
   it's used in further math, so precision loss doesn't leak into the final PMT.
4. **Last installment absorbs the rounding remainder.** Whatever repayment method,
   the final row must set `remainingBalance` to exactly zero — recompute the last
   principal/total payment from the actual remaining balance rather than trusting
   the formula's rounded output. This is what makes the schedule reconcile.
5. Repayment method formulas:
   - **원리금균등 (equal principal & interest)**: `PMT = P·r·(1+r)^n / ((1+r)^n - 1)`,
     same total payment every month, principal portion grows over time.
   - **원금균등 (equal principal)**: fixed principal `P/n` each month, interest shrinks
     as balance shrinks, so total payment decreases over time.
   - **만기일시 (bullet/maturity)**: interest-only every month, full principal due in
     the final installment alongside that month's interest.

## UI / screen design

Every screen in this project (이자계산, 상환스케줄 보기, and any new ones) follows the design
tokens in [DESIGN.md](../../../DESIGN.md) at the repo root — a verified Toss TDS Mobile
reference (colors, typography, spacing, radius, component states). Read it before styling
anything, and treat its tokens as the single source of truth rather than inventing new
colors, fonts, or one-off component geometry per screen:

- **Colors**: primary `#3182f6` / hover `#2272eb`, foreground `#191f28`, body `#4e5968`,
  muted `#8b95a1`, surface `#f2f4f6`, border `#e5e8eb`, weak-background `#e8f3ff` /
  weak-foreground `#1b64da` (info/active states), danger `#e42939` (overdue/errors).
- **Typography**: `Toss Product Sans` with a system-font fallback chain; body 16px/400,
  body-small 14px/400 for most UI text, the h1–h4 scale only for page/section titles.
- **Spacing/radius**: the 4/6/8/16/24/32px spacing scale and 4/6/10/14/16px radius scale
  (16px + 56px height for xlarge primary buttons, matching the TDS button spec).
- Don't merge TDS mobile geometry with invented "generic fintech" shadows, cards, or
  animations that DESIGN.md explicitly doesn't document — extend deliberately and note it
  as an extension rather than presenting it as verified TDS, per DESIGN.md's own rules.

`static/index.html` in `loan-engine-service` already implements these tokens as CSS
variables (`--color-primary`, `--space-*`, `--radius-*`, etc.) — reuse that variable set
for any new screen instead of redefining the palette.

## Development roadmap

1. `docker-compose up -d` to bring up PostgreSQL.
2. Build `loan-engine-service`: port the three methods from `LoanCalculator.java`,
   write unit tests that assert the schedule reconciles (sum of principal payments
   == original principal, final balance == 0), then expose via REST controller.
3. Build `loan-schedule-service`: JPA entities + repositories for the two tables,
   Feign/WebClient client to call the engine service, and a `@Transactional` loan
   application endpoint that persists contract + full schedule together.
4. Verify end-to-end with Postman/curl: create a loan, confirm the persisted schedule
   in Postgres matches what the engine returned, and that balances zero out exactly
   at the last row.

When picking up new work here, check which roadmap step the repo is currently at
(e.g. does `loan-engine-service` exist yet, does it have all three repayment
methods, is `loan-schedule-service` wired to it) before assuming the next step.
