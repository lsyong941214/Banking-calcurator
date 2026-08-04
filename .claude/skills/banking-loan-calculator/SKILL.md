---
name: banking-loan-calculator
description: Reference design for the "금융개발" (financial development) work in this project — a Java/Spring Boot MSA loan calculator, built deliberately as an MSA-practice project (not because the app needs MSA), split strictly by business function. Consult this whenever working in this repo on anything related to loans, interest/repayment calculation, loan-calcurator, loan-engine-service, loan-schedule-service, loan-code-service, loan-ledger-service, the code_item/product_*_rule schema, the lon_acct_base ledger, the git commit/push workflow, or any screen/UI work. Always check this skill before writing new Java classes, DB migrations, docker-compose changes, adding a new service module, or HTML/CSS for a screen here, even if the request only mentions one small piece (e.g. "add a new repayment type", "fix the interest rounding", or "add a field to the screen") — it defines the conventions and design system the whole codebase must stay consistent with.
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
- **Live access points**: Render (always-on) at https://banking-calcurator.onrender.com,
  and ngrok (only while the local Mac is running) at
  https://whimsical-unadvised-divisibly.ngrok-free.dev — see README.md. **As of
  2026-07-29 this Render service is mid-transition**: it was built from
  `loan-engine-service/Dockerfile` back when that module also served the screen.
  `loan-engine-service` is now API-only (screen moved to `loan-calcurator`), so whatever
  is actually live at that address depends on whether/how the Render service's root
  directory has been repointed — check before assuming it shows a working screen.
  Deploying `loan-calcurator` (and giving `loan-engine-service`/`loan-schedule-service`/
  `loan-code-service`/`loan-ledger-service` their own Render services, all 4 of which are
  now defined in `render.yaml`) requires the Render dashboard — no API token or CLI is
  set up in this environment, so that step needs the user or a connected browser session,
  not a git push alone. There's also no Supabase CLI/API token here — `db/init_ledger.sql`
  was applied to the Supabase project directly via `psql` with a password the user
  provided in chat, not through any stored credential.
- **Auto-restart on commit**: `.git/hooks/post-commit` (source tracked at
  `scripts/git-hooks/post-commit`, since git doesn't version `.git/hooks/`) rebuilds
  and restarts the locally-running `loan-engine-service` in the background whenever a
  commit touches that directory. **It does not yet watch `loan-calcurator/`,
  `loan-schedule-service/`, `loan-code-service/`, or `loan-ledger-service/`** — restart
  those manually (`./mvnw -q -B clean package -DskipTests && java -jar target/*.jar`)
  after pulling changes that touch them, or extend the hook if this becomes annoying.

## Architecture

Five units today, strictly split by business function — not by technical layer (see
below for why that distinction matters here):

```
                              [Browser: loan-calcurator, 8080]
                               (static screen only, no logic)
                    /              |                 \                 \
                   ▼               ▼                  ▼                  ▼
     [loan-engine-service   [loan-schedule-service  [loan-code-service  [loan-ledger-service
      8081, no DB]           8083, no DB]            8082, owns          8084, owns Postgres
      이자계산 only            상환스케줄 미리보기 only    PostgreSQL]         (same Supabase
                                                       상품/상환방식/       instance as
                                                       대출기간 유효값 조회  loan-code-service)]
                                                                          원장조회
                                                                          (lon_acct_base) only
```

- **loan-calcurator** (port 8080): the entire UI, and *only* the UI — static HTML/CSS/JS
  (`src/main/resources/static/index.html`), no business logic, no DB. On load it calls
  loan-code-service for valid values; on "계산하기" it calls loan-engine-service; on
  "상환스케줄 미리보기" it calls loan-schedule-service; on "원장조회" it calls
  loan-ledger-service, and its "이자계산에 사용" button copies a looked-up account's
  dates/rates/balance/repayment method straight into the 이자계산 form so the two screens
  work together without a shared backend. All four calls are cross-origin from the browser
  (different ports/hosts), so each of the four backend services needs CORS configured for
  whatever origin the screen is served from.
- **loan-engine-service** (port 8081): stateless REST API, **이자계산 only** — given
  newDate/maturityDate/rates/principal/etc., returns the interest/fee breakdown for all
  three repayment methods. No DB, no UI, nothing else. Don't add schedule generation or
  any other business function back into this module — that's the whole point of the split.
- **loan-schedule-service** (port 8083): stateless REST API, **상환스케줄 미리보기 only** —
  given the same kind of inputs, returns the full amortization schedule (one row per
  installment) for all three repayment methods. No DB. Extracted from
  `loan-engine-service` on 2026-07-29 once it was pointed out that "read code values from
  a DB" and "generate a repayment schedule" are different business functions and neither
  belongs bundled with 이자계산.
- **loan-code-service** (port 8082): owns PostgreSQL. Serves code-table lookups
  (`GET /api/v1/codes/product-options`, `/repayment-types`) — valid-value rules that used
  to be hardcoded in the frontend JS. **This is not the ledger.**
- **loan-ledger-service** (port 8084): owns the `lon_acct_base` (대출계좌원장) table on the
  same Postgres instance as loan-code-service. Serves `GET /api/v1/ledger/accounts` (with
  an optional `keyword` filter matched against `acct_no`/`cust_no`) — **원장조회 only**, no
  write endpoints, no other business function. Added 2026-08-02 as the answer to the
  "which service owns the ledger?" question this file used to leave open — see Database
  schema below for how `lon_acct_base` relates to the older deferred ledger sketch. Also
  serves `GET /api/v1/ledger/accounts/search?custNo=&custName=&acctStatCd=` (added
  2026-08-04) for the account-search popup shared by 이자계산 and 원장조회 (see UI section) —
  AND-combined optional filters (blank/omitted param = ignored), distinct from the
  OR-based `keyword` param on `/accounts` which the main 원장조회 search box still uses.
- **PostgreSQL**: chosen specifically for `NUMERIC` precision and transactions — required
  for money, never optional.

**Why business-function splitting, not technical-layer splitting:** an earlier version of
this project split `loan-schedule-service` off purely because it "had a DB" while
`loan-engine-service` didn't — i.e. split along a technical seam (stateless compute vs.
persistence) rather than a business one. That's a known MSA anti-pattern: the two pieces
still change together, deploying one without the other has no real benefit, and it adds a
chatty network hop for no reason. The correct cut, confirmed with the user 2026-07-29, is
by business capability — 이자계산, 상환스케줄 미리보기, 코드값 조회, and (since 2026-08-02)
원장조회 are each their own service regardless of whether they touch a database. Keep using
that test (`does this represent a distinct business capability?`, not `does this need a
DB?`) for any future service split.

**This is a deliberate MSA-practice project, not a "needs MSA" production app.** The split
exists so the user can practice modifying/building/deploying services independently — not
because traffic or team scale demands it. Don't suggest collapsing it back into one
service. Do keep any new business capability in its own new sibling module under
`int_calc/` (own `pom.xml`, own build/deploy) rather than folded into an existing one.

Actual module layout:

```
int_calc/
├── docker-compose.yml            # local Postgres (postgres:15-alpine), seeded by db/init.sql + db/init_ledger.sql
├── db/init.sql                   # loan-code-service's schema + seed data (no migration tool -- demo project)
├── db/init_ledger.sql            # loan-ledger-service's schema (lon_acct_base) + seed data
├── loan-calcurator/               # port 8080 -- screen only
│   └── src/main/resources/static/index.html
├── loan-engine-service/          # port 8081 -- 이자계산 only
│   └── src/main/java/com/example/engine/
│       ├── controller/           # POST /api/v1/interest-calculations
│       ├── service/              # InterestCalculationService
│       └── config/               # CorsConfig (allows the loan-calcurator origin)
├── loan-schedule-service/        # port 8083 -- 상환스케줄 미리보기 only
│   └── src/main/java/com/example/schedule/
│       ├── controller/           # POST /api/v1/repayment-schedules
│       ├── service/              # RepaymentScheduleService (amortization schedule generation)
│       └── config/               # CorsConfig
├── loan-code-service/            # port 8082 -- owns Postgres
│   └── src/main/java/com/example/code/
│       ├── controller/           # GET /api/v1/codes/*
│       ├── service/              # ProductOptionService (assembles per-product bundles)
│       ├── domain/               # CodeGroup, CodeItem, ProductRepaymentTypeRule, ProductLoanTermRule
│       ├── repository/           # Spring Data JPA repositories
│       └── config/               # CorsConfig
└── loan-ledger-service/          # port 8084 -- owns lon_acct_base on the same Postgres/Supabase
    └── src/main/java/com/example/ledger/
        ├── controller/           # GET /api/v1/ledger/accounts?keyword=
        ├── service/              # LedgerAccountService
        ├── domain/               # LonAcctBase, LonAcctBaseId (composite key: acctNo+acctSeqNo)
        ├── repository/           # LonAcctBaseRepository
        └── config/               # CorsConfig
```

## Database schema

**Implemented now** (owned by `loan-code-service`): [../../../db/init.sql](../../../db/init.sql)
at the repo root — `code_group` + `code_item` (grouped valid values: `PRODUCT_TYPE`,
`REPAYMENT_TYPE`), plus two flat rule tables, `product_repayment_type_rule` and
`product_loan_term_rule`, mapping which repayment types / loan terms are valid per product.
No FK from the rule tables to `code_item` (kept simple for a demo), and no migration tool —
schema changes go straight into `db/init.sql`; applying a change to an existing local DB means
dropping the docker volume (or hand-applying the diff) rather than a versioned migration.

**Implemented now** (owned by `loan-ledger-service`): [../../../db/init_ledger.sql](../../../db/init_ledger.sql)
at the repo root — `lon_acct_base` (대출계좌원장), PK'd on `(acct_no, acct_seq_no)` because
`acct_no` alone isn't unique (중도금대출 등 회차별로 같은 계좌번호 아래 여러 `acct_seq_no`가 붙는다).
Money columns (`loan_limit_amt`, `loan_bal_amt`) are `NUMERIC(15,0)` (whole won, matching
`loan-engine-service`'s `MONEY_SCALE = 0`); rate columns (`base_rate`, `add_rate`,
`apply_rate`, `early_repay_fee_rate`) are `NUMERIC(8,6)` decimal fractions (e.g. `0.035000`
= 3.5%), matching `appliedRate`'s `setScale(6, ...)` in `InterestCalculationService`. Date
columns are `VARCHAR(8)` `YYYYMMDD` strings by design (원장 시스템 정합성), not SQL `DATE`.
`acct_stat_cd`/`item_cd`/`repay_method_cd` are `CHECK`-constrained and documented via
`COMMENT ON COLUMN`. No audit columns (`created_at`/`updated_at`) — kept consistent with
`code_item`/`code_group`, which don't have them either.

`cust_name` (added 2026-08-04) lives directly on `lon_acct_base` rather than a separate
customer table — there's no customer service/table anywhere in this project, and adding one
just to hold a name would contradict the "single table, not two FK'd tables" shape this
ledger deliberately took (see below). Needed for the account-search popup's 고객명 filter.

`rate_change_type_cd` (금리변동구분코드, added 2026-08-04) is `01`=고정금리 / `02`=변동금리,
`CHECK`-constrained like the other `_cd` columns. `rate_change_cycle` (금리변동주기, 개월수 —
e.g. `'03'`/`'06'`/`'12'`) is nullable and `CHECK`-tied to it: `NULL` when `01`(고정), required
when `02`(변동) — see `ck_lon_acct_base_rate_change_cycle`. Both are read/display-only for now:
`loan-engine-service`/`loan-schedule-service` still calculate off a single `apply_rate` for the
whole term and don't yet model a rate actually changing mid-schedule — adding that behavior is
a separate, bigger change (schedule generation would need to re-derive the rate at each cycle
boundary) and hasn't been requested/built.

This **is** the ledger the rest of this file used to describe as deferred with an undecided
owner — resolved 2026-08-02: `loan-ledger-service` owns it. Note it is a different, simpler
shape than the sketch below (single table, not two FK'd tables) — the sketch was written
before real requirements existed and is now superseded for the account-level ledger. It may
still be relevant later if per-installment history (one row per payment) is needed, which
`lon_acct_base` does not attempt to track.

**Deferred, sketch only** (not built): [references/schema.sql](references/schema.sql) — the
original `loan_contract`/`repayment_schedule` design (one row per loan, one row per
installment, FK'd together) for per-installment payment history. Superseded for
account-level data by `lon_acct_base` above; only build this if per-installment history is
actually needed later, and ask which service should own it before doing so.

**Every money or rate column must be `NUMERIC`, never `FLOAT`/`DOUBLE`** — non-negotiable for
financial data, floating point drift compounds across months and produces balances that
don't reconcile to zero at maturity.

`repayment_type` is one of `EQUAL_PRINCIPAL_AND_INTEREST` (원리금균등), `EQUAL_PRINCIPAL`
(원금균등), or `BULK` (만기일시) — reflected in `code_item` seed data, and required with the
same rigor (not just the first case) in both `loan-engine-service` and `loan-schedule-service`'s
calculation logic (each keeps its own small copy of the `RepaymentType` enum — no shared
library between services, by design, for real independence).

Local DB comes up via `docker-compose up -d` at the repo root (postgres:15-alpine, db `loandb`,
mounts both `db/init.sql` and `db/init_ledger.sql`). **This dev Mac doesn't have Docker
installed** — verified the schema and `loan-code-service`/`loan-ledger-service` end-to-end via
a manually-started Homebrew `postgresql@15` instance instead (same `loandb`/`loanuser`/
`loanpassword` creds as `docker-compose.yml`, so switching to real Docker later needs no config
changes; check `pg_isready -p 5432` before assuming it's still up in a new session).
**Production DB is Supabase-hosted Postgres** (not a Render-managed database) — both
`loan-code-service` and `loan-ledger-service` point at the same Supabase project via
`SPRING_DATASOURCE_*` env vars wired in `render.yaml` (credentials marked `sync: false`, so
Render prompts for them rather than storing them in the blueprint file). `db/init_ledger.sql`
has already been applied directly to that Supabase project (via `psql`, since there's no
Supabase CLI/API token set up here) with 5 seed rows — re-running it against a fresh Supabase
project needs the same manual `psql` step.

## Java calculation rules

Reference implementation: [references/LoanCalculator.java](references/LoanCalculator.java).
It covers all three repayment methods — `loan-engine-service` (이자계산) and
`loan-schedule-service` (상환스케줄 생성) each implement their own subset of this logic; use
it as the reference for both, not just the equal-principal-and-interest case shown in early
drafts.

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

Every screen in this project (이자계산 — including its inline 상환스케줄 미리보기 panel — and
any new ones) lives in `loan-calcurator` and follows the design tokens in
[DESIGN.md](../../../DESIGN.md) at the repo root — a verified Toss TDS Mobile reference
(colors, typography, spacing, radius, component states). Read it before styling anything,
and treat its tokens as the single source of truth rather than inventing new colors, fonts,
or one-off component geometry per screen:

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

`static/index.html` in `loan-calcurator` already implements these tokens as CSS variables
(`--color-primary`, `--space-*`, `--radius-*`, etc.) — reuse that variable set for any new
screen instead of redefining the palette. The screen calls four separate backend origins
(`ENGINE_SERVICE_BASE`, `SCHEDULE_SERVICE_BASE`, `CODE_SERVICE_BASE`, `LEDGER_SERVICE_BASE`
constants near the top of the `<script>`) — keep that pattern for any new screen that needs
its own services. All screens/views live in the single `index.html` as sidebar-switched
`.view` sections (see `#view-interest` / `#view-ledger`), not separate HTML files — follow
that pattern for any new screen too, and expose a `window.*` hook (like
`window.fillInterestFormFromLedger`) from a view's IIFE if another view needs to feed it data.

**Shared account-search popup** (added 2026-08-04): `#acctSearchModalOverlay` is one modal
shared by both views via `window.openAccountSearchModal(onSelect)` — call it with a callback
that receives the full selected account object (not just the acct number), so the caller can
both fill its own 계좌번호 field and, on the 이자계산 side, feed the account straight into
`fillInterestFormFromLedger` without a second round trip. It queries
`loan-ledger-service`'s AND-filtered `/accounts/search` endpoint (고객번호/고객명/계좌상태),
distinct from the OR-based `keyword` search the main 원장조회 box uses. Any future view that
needs "pick an account" should reuse this modal/callback pattern rather than building another
one. `rateChangeInfoText(account)` (top-level helper, next to the modal IIFE) renders
금리변동구분/주기 as one label (e.g. "변동금리 (6개월 주기)" / "고정금리") — reuse it anywhere
an account's rate-change info needs to be shown rather than re-deriving it inline.

## Development roadmap

Status as of 2026-08-04:

1. ✅ `loan-calcurator` — screen extracted from `loan-engine-service`, calls all four
   backend services from the browser.
2. ✅ `loan-engine-service` — narrowed to 이자계산 only (schedule generation moved out).
3. ✅ `loan-schedule-service` — 상환스케줄 미리보기, extracted from `loan-engine-service`.
4. ✅ `loan-code-service` — owns Postgres, serves product/repayment-type/loan-term code
   tables to the screen (with a hardcoded-JS fallback if it's unreachable).
5. ✅ `loan-ledger-service` — owns `lon_acct_base`, serves 원장조회 to the screen; the
   screen's "이자계산에 사용" button feeds a looked-up account into the 이자계산 form.
   Schema applied to both local Postgres and the production Supabase project (5 seed rows).
6. ✅ Account-search popup (2026-08-04) — shared modal on both 이자계산/원장조회 views,
   backed by `loan-ledger-service`'s new `/accounts/search` endpoint (고객번호/고객명/계좌상태).
   `lon_acct_base` gained `cust_name`, `rate_change_type_cd`(01 고정/02 변동), `rate_change_cycle`
   (개월수, 변동금리일 때만) — applied to local Postgres; **not yet applied to the production
   Supabase project** (same manual-`psql` step as the original ledger schema — see Database
   schema above — needs to be re-run there before the next Supabase-backed deploy picks it up).
7. ⏳ In progress / not started: `render.yaml` now lists all 4 backend services
   (including `loan-ledger-service`) but the Render dashboard step to actually apply
   the blueprint/repoint the live deployment hasn't happened yet (see "Live access
   points" above); auth/per-user screen permissions; a migration tool; the
   per-installment ledger sketch (owner TBD, see Database schema above); extending
   `.git/hooks/post-commit` to cover the four new modules; write endpoints for
   `lon_acct_base` (원장조회 is currently read-only); actually using `rate_change_type_cd`/
   `rate_change_cycle` in interest/schedule calculation (currently display-only, see
   Database schema above).

When picking up new work here, check which of these is actually true in the repo before
assuming the next step — don't trust an older draft of this list over what you actually find.
