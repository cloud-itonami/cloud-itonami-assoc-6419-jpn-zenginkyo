# ADR-0002 — record `addressee` and `parameter-disposition`, not just the citation

- Status: accepted
- Date: 2026-07-29
- Upstream: `com-junkawasaki/root` ADR-2607284000 (corporate vishing fraud —
  system dynamics and interventions)

## Context

ADR-2607284000 modelled a corporate vishing case in which ¥1.179bn left a
company in a 23.3-hour window. Two of its computed findings bear directly
on this catalog:

1. The single-transfer limit did not bind. Transfers were already
   structured just under the threshold the bank itself named. Only a
   **cumulative daily** cap changes the outcome, and the account's actual
   limit was the shipped default — ¥999,999,999,999, about 282× the firm's
   estimated assets.
2. A single firm cannot compute its own exposure. Reading zero incidents
   in 25 company-years as "our rate is low" is a measurement error; at 95%
   confidence, 25 company-years of silence is consistent with an annual
   rate of 11.29%. Claiming ≤1% needs **299 incident-free company-years**,
   which one firm structurally cannot accumulate.

Together those say the transfer limit is a parameter that matters and
that the party currently holding it cannot compute. That made the
association layer worth checking, so the ADR ranked "does zenginkyo set a
default?" as a band/B intervention and left it unimplemented.

## What the record actually says

Four zenginkyo documents were fetched live on 2026-07-29. The parameter is
named, repeatedly, over a decade — and every time the number is handed to
someone else:

| Document | Date | Addressee | Disposition |
|---|---|---|---|
| 法人向けインターネット・バンキングに係る預金等の不正な払戻しへの対応について | 2014-05-15 | member banks | delegates to adopter |
| 法人向けインターネット・バンキングにおける預金等の不正な払戻しに関する補償の考え方について | 2014-07-17 | member banks | conditions liability on adopter's choice |
| 金融犯罪への対応の徹底に係る申し合わせについて | 2024-05-10 | member banks | no numeric parameter at all |
| 法人向けインターネット・バンキングにおける不正送金にご注意！ | not shown | corporate customers | delegates to adopter |

The 2014-05-15 arrangement is addressed to member banks but its
transfer-limit clause — 「振込・払戻し等の限度額を必要な範囲内でできるだけ低く
設定する」 — is an instruction *to the customer*. The customer-facing alert
repeats it. The 2014-07-17 compensation arrangement then makes compensation
turn on whether the customer implemented the 別紙1 measures, of which that
limit is one.

So the association names the parameter, declines to fix it, routes it to
the customer, and attaches a consequence to how the customer set it. The
2024 arrangement, the most recent association-level commitment on
financial crime, contains no numeric threshold of any kind.

## Decision

Add the four documents, and record two new fields on every entry:

- `:association-rule/addressee` — `:member-bank` | `:corporate-customer` |
  `:general-public`
- `:association-rule/parameter-disposition` — `:sets-numeric-default` |
  `:delegates-to-adopter` | `:conditions-liability-on-adopter-choice` |
  `:no-parameter`

Both classify the document. Neither stores rule text, so the repo's
citation-metadata-only license posture is unchanged.

Export `disposition-count` and `sets-numeric-default-count`, both scanning
the same tables the field accessors read. Counts are derived rather than
restated so that adding an entry cannot leave a summary behind.

## Consequences

`(sets-numeric-default-count "zenginkyo" "transfer-limit")` returns 0 —
the ADR's band/B question now has a machine-readable answer instead of an
assumption. `disposition-count` for `delegates-to-adopter` returns 2.

**This is an absence in the published record, not a recommendation.** The
catalog reports what the association has published and takes no position
on what it ought to publish; whether an association *should* default a
member's customer-facing parameter is a governance question this repo has
no standing to answer. If a future document fixes a number, adding it
moves the count off zero, and the test asserting zero is what has to be
updated to say so.

Two limits worth stating. The undated customer-facing alert is stored with
a nil `established-date` because the page shows none — inferring one from
sibling pages would be fabrication. And `parameter-disposition` is a
judgement about a document, reproducible from the cited URL but still a
judgement; the URL is stored precisely so it can be re-checked rather than
trusted.
