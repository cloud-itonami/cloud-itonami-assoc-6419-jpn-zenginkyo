# cloud-itonami-assoc-6419-jpn-zenginkyo

Industry self-regulatory rule catalog for the **Japanese Bankers
Association** (一般社団法人 全国銀行協会 / zenginkyo) — the
industry-association-level counterpart to
[`cloud-itonami-iso3166-jpn`](https://github.com/cloud-itonami/cloud-itonami-iso3166-jpn)'s
`statute.facts` (national law) and
[`cloud-itonami-municipality-jpn-tokyo`](https://github.com/cloud-itonami/cloud-itonami-municipality-jpn-tokyo)'s
`ordinance.facts` (municipal). Part of the
[`cloud-itonami`](https://github.com/cloud-itonami) compliance-fact family
(ADR-2607141700, `cloud-itonami-compliance-fact-federation`, in
`com-junkawasaki/root`).

Aligned to **ISIC 6419** (banking), one of the 12 verticals already wired
into `cloud-itonami-isic-8291`'s compliance-intelligence links
(ADR-2607110400).

## Scope

A **read-only reference/archive** catalog — not an Advisor⊣Governor
actuation actor, same class as `cloud-itonami-gtin-catalog` /
`cloud-itonami-lei-*` / `cloud-itonami-municipality-jpn-tokyo`. It
proposes or executes nothing on the association's behalf.

Coverage is reported honestly by the fail-closed exported Kotoba ABI: an
association not explicitly admitted has **no spec-basis**, full stop — never
fabricate one.

## Data

- `src/association_facts.kotoba` — the sole production catalog authority.
- `schema/association-rule.edn` — DataScript schema.
- `data/datascript-tx.edn` — derived DataScript tx-data (query this
  alongside other `cloud-itonami`/`etzhayyim` compliance-fact sources via
  `com-junkawasaki/root`'s `scripts/compliance-fact-query.cljs`). Derived
  means derived: `datascript-tx-matches-kotoba-authority` fails if this
  file drifts from the compiled catalog, so it is not a second place to
  add an entry.

Every entry cites an official [zenginkyo.or.jp](https://www.zenginkyo.or.jp/)
page — each URL was independently fetched and its title/revision-history
verified against the live document (entries 1–2 on 2026-07-14, entries
3–6 on 2026-07-29), never guessed from memory.

## `parameter-disposition` — who ends up holding the number

Beyond citation metadata, each entry records **who the rule speaks to**
(`:addressee`) and **what it does with any numeric parameter it raises**
(`:parameter-disposition`, one of `:sets-numeric-default`,
`:delegates-to-adopter`, `:conditions-liability-on-adopter-choice`,
`:no-parameter`). Both classify the published document, not its text —
this repo still stores no rule text.

They exist because "the association addressed topic X" and "the
association decided the number for topic X" are different facts, and a
catalog that stores only the citation cannot tell them apart. Counts are
scanned from the same tables the fields come from, so a summary cannot go
stale against the catalog it summarizes:

```clojure
(sets-numeric-default-count "zenginkyo" "transfer-limit")               ;=> 0
(disposition-count "zenginkyo" "transfer-limit" "delegates-to-adopter") ;=> 2
```

Read that as an absence in the published record, not as a
recommendation. The catalog reports what zenginkyo has published and
takes no position on what it ought to publish. If a future document does
fix a number, adding it moves the count off zero — and the test asserting
zero is then the thing that has to be updated to say so.

The catalog compiles through `kotoba-lang/compiler` to the reference evaluator,
restricted JavaScript, and typed WebAssembly. Clojure/JVM and Node are test and
compiler hosts only; neither is production authority. Compatibility is checked
by observable values, typed ABI, empty effects, bounds, and fail-closed
rejections—not compiler-output byte identity.

## License

AGPL-3.0-or-later (matches the `cloud-itonami-iso3166-*` /
`-municipality-*` / `-lei-*` convention). Rule text itself remains the
association's; this repo stores only citation metadata (id/title/url/
dates), not full rule text.
