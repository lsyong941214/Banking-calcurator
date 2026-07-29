---
name: banking-loan-calculator
description: Reference design for the "금융개발" (financial development) work in this project — a Java/Spring Boot MSA loan calculator, built deliberately as an MSA-practice project (not because the app needs MSA). Consult this whenever working in this repo on anything related to loans, interest/repayment calculation, loan-engine-service, loan-code-service, the code_item/product_*_rule schema (or the still-deferred loan_contract/repayment_schedule ledger), the git commit/push workflow, or any screen/UI work (이자계산, or new screens). Always check this skill before writing new Java classes, DB migrations, docker-compose changes, adding a new service module, or HTML/CSS for a screen here, even if the request only mentions one small piece (e.g. "add a new repayment type", "fix the interest rounding", or "add a field to the screen") — it defines the conventions and design system the whole codebase must stay consistent with.
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
- **Live access points**: Render (always-on) at
  https://banking-calcurator.onrender.com, and ngrok (only while the local Mac is
  running) at https://whimsical-unadvised-divisibly.ngrok-free.dev — see README.md.
- **Auto-restart on commit**: `.git/hooks/post-commit` (source tracked at
  `scripts/git-hooks/post-commit`, since git doesn't version `.git/hooks/`) rebuilds
  and restarts the locally-running `loan-engine-service` in the background whenever a
  commit touches that directory, so the ngrok tunnel keeps serving current code
  without a manual restart. No action needed after committing — just be aware the
  local server briefly bounces a few seconds after each relevant commit.

## Architecture

Two services today; a third (the ledger) is deferred and its home isn't decided yet.

```
              [Browser]
             /         \
            ▼           ▼
   [Loan Engine        [Loan Code
    Service]            Service]  <--->  [PostgreSQL]
   (8081, no DB,        (8082, owns DB)
    serves the UI)
```

- **loan-engine-service** (port 8081): stateless REST API + the static UI
  (`src/main/resources/static/index.html`). Pure repayment-method math, no DB access, no
  persistence. On page load, its UI calls loan-code-service for product/repayment-type/
  loan-term valid values, falling back to hardcoded JS defaults if that call fails.
- **loan-code-service** (port 8082): owns PostgreSQL. Currently serves only code-table
  lookups (`GET /api/v1/codes/product-options`, `/repayment-types`) — the valid-value rules
  that used to be hardcoded in the frontend JS. **Renamed from `loan-schedule-service` on
  2026-07-29** once "schedule" turned out to oversell what it actually does (it doesn't touch
  the 상환스케줄 미리보기 feature at all — that's pure calculation inside loan-engine-service).
  Whichever service ends up owning the ledger (`loan_contract`/`repayment_schedule`, still
  deferred) may or may not be this one — that's genuinely undecided, don't assume it and don't
  build it without asking first.
- **PostgreSQL**: chosen specifically for `NUMERIC` precision and transactions — required for
  money, never optional, even though nothing money-shaped is persisted yet.

**This is a deliberate MSA-practice project, not a "needs MSA" production app.** The split
exists so the user can practice modifying/building/deploying services independently — not
because traffic or team scale demands it. Don't suggest collapsing it back into one service.
Do keep any new capability (e.g. a hypothetical future 계좌조회 feature) in its own new sibling
module under `int_calc/` (own `pom.xml`, own build/deploy) rather than folded into an existing
service — that boundary is the point of the exercise.

Actual module layout:

```
int_calc/
├── docker-compose.yml          # local Postgres (postgres:15-alpine), seeded by db/init.sql
├── db/init.sql                 # code tables' schema + seed data (no migration tool -- demo project)
├── loan-engine-service/        # port 8081
│   ├── src/main/java/com/example/engine/
│   │   ├── controller/         # POST /api/v1/interest-calculations, /repayment-schedules
│   │   └── service/            # repayment-method algorithms
│   └── src/main/resources/static/index.html   # the entire UI
└── loan-code-service/          # port 8082
    └── src/main/java/com/example/code/
        ├── controller/         # GET /api/v1/codes/*
        ├── service/            # ProductOptionService (assembles per-product bundles)
        ├── domain/             # CodeGroup, CodeItem, ProductRepaymentTypeRule, ProductLoanTermRule
        └── repository/         # Spring Data JPA repositories
```

## Database schema

**Implemented now** (owned by `loan-code-service`): [../../../db/init.sql](../../../db/init.sql)
at the repo root — `code_group` + `code_item` (grouped valid values: `PRODUCT_TYPE`,
`REPAYMENT_TYPE`), plus two flat rule tables, `product_repayment_type_rule` and
`product_loan_term_rule`, mapping which repayment types / loan terms are valid per product.
No FK from the rule tables to `code_item` (kept simple for a demo), and no migration tool —
schema changes go straight into `db/init.sql`; applying a change to an existing local DB means
dropping the docker volume (or hand-applying the diff) rather than a versioned migration.

**Deferred, sketch only** (not built): [references/schema.sql](references/schema.sql) — the
original `loan_contract`/`repayment_schedule` ledger design (one row per loan, one row per
installment, FK'd together). The user confirmed this comes later; don't build it unless asked,
and don't assume it lands in `loan-code-service` just because that was the plan under the old
`loan-schedule-service` name — that's still an open decision.

**Every money or rate column must be `NUMERIC`, never `FLOAT`/`DOUBLE`**, once the ledger
exists — non-negotiable for financial data, floating point drift compounds across months and
produces balances that don't reconcile to zero at maturity.

`repayment_type` is one of `EQUAL_PRINCIPAL_AND_INTEREST` (원리금균등), `EQUAL_PRINCIPAL`
(원금균등), or `BULK` (만기일시) — already reflected in `code_item` seed data and required with
the same rigor (not just the first case) in `loan-engine-service`'s calculation logic.

Local DB comes up via `docker-compose up -d` at the repo root (postgres:15-alpine, db `loandb`,
mounts `db/init.sql`). **This dev Mac doesn't have Docker installed** — verified the schema and
`loan-code-service` end-to-end via a manually-started Homebrew `postgresql@15` instance instead
(same `loandb`/`loanuser`/`loanpassword` creds as `docker-compose.yml`, so switching to real
Docker later needs no config changes; check `pg_isready -p 5432` before assuming it's still up
in a new session). Production DB is planned to be a managed Postgres add-on (e.g. Render's),
not a self-hosted container.

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

Every screen in this project (이자계산 — including its inline 상환스케줄 미리보기 panel — and any
new ones) follows the design
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

Status as of 2026-07-29:

1. ✅ `loan-engine-service` — grown well past the original three-method sketch: product
   types, early-repayment-fee exemption rules, mobile nav drawer, an inline 상환스케줄 미리보기
   panel (calls its own `/api/v1/repayment-schedules`, no DB involved).
2. ✅ `loan-code-service` — owns Postgres, serves product/repayment-type/loan-term code
   tables to `loan-engine-service`'s UI (with a hardcoded-JS fallback if it's unreachable).
   Runs locally only; not deployed anywhere yet (Render only hosts `loan-engine-service`).
3. ❌ Not started: the ledger (`loan_contract`/`repayment_schedule` persistence, owner TBD),
   auth/per-user screen permissions, a migration tool, deploying `loan-code-service`, and
   extending `.git/hooks/post-commit` to rebuild `loan-code-service` too (it currently only
   watches `loan-engine-service/`).

When picking up new work here, check which of these is actually true in the repo before
assuming the next step — don't trust an older draft of this list over what you actually find.
