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

Coverage is reported honestly (see `association.facts/coverage`): an
association not in `catalog` has **no spec-basis**, full stop — never
fabricate one.

## Data

- `src/association/facts.cljc` — the catalog, source of truth.
- `schema/association-rule.edn` — DataScript schema.
- `data/datascript-tx.edn` — derived DataScript tx-data (query this
  alongside other `cloud-itonami`/`etzhayyim` compliance-fact sources via
  `com-junkawasaki/root`'s `scripts/compliance-fact-query.cljs`).

Every entry cites an official [zenginkyo.or.jp](https://www.zenginkyo.or.jp/)
page — each URL was independently fetched and its title/revision-history
verified against the live document (2026-07-14), never guessed from
memory.

## License

AGPL-3.0-or-later (matches the `cloud-itonami-iso3166-*` /
`-municipality-*` / `-lei-*` convention). Rule text itself remains the
association's; this repo stores only citation metadata (id/title/url/
dates), not full rule text.
