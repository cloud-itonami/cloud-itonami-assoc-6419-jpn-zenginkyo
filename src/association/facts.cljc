(ns association.facts
  "Industry self-regulatory rule catalog for the Japanese Bankers Association
  (一般社団法人 全国銀行協会 / JBA, zenginkyo) -- the industry-association-level
  counterpart to cloud-itonami-iso3166-jpn's `statute.facts` (national law) and
  cloud-itonami-municipality-jpn-tokyo's `ordinance.facts` (municipal). Per
  ADR-2607141700 (cloud-itonami-compliance-fact-federation): aligned 1:1 to
  ISIC 6419 (banking), already wired into cloud-itonami-isic-8291's
  compliance-intelligence links. Every entry cites an OFFICIAL zenginkyo.or.jp
  URL -- never fabricated. A rule not in this table has NO spec-basis, full
  stop; extend `catalog`, do not invent an id/url.

  Every URL below was independently WebFetch-verified against the live
  zenginkyo.or.jp page on 2026-07-14 (title + revision history read back
  from the actual document, not guessed).")

(def catalog
  "assoc-slug -> vector of self-regulatory rule entries."
  {"zenginkyo"
   [{:association-rule/id "zenginkyo.code-of-conduct"
     :association-rule/title "Code of Conduct (倫理憲章)"
     :association-rule/association "zenginkyo"
     :association-rule/isic "6419"
     :association-rule/country "JPN"
     :association-rule/kind :self-regulatory-code
     :association-rule/url "https://www.zenginkyo.or.jp/en/conduct/"
     :association-rule/url-provenance :official-association-site
     :association-rule/established-date "2005-11-22"
     :association-rule/last-revised-date "2022-09-15"
     :association-rule/retrieved-at "2026-07-14"
     :association-rule/topic #{:ethics :member-conduct}}
    {:association-rule/id "zenginkyo.agreements"
     :association-rule/title "申し合わせ (Board-resolved Member-Bank Arrangements)"
     :association-rule/association "zenginkyo"
     :association-rule/isic "6419"
     :association-rule/country "JPN"
     :association-rule/kind :self-regulatory-code
     :association-rule/url "https://www.zenginkyo.or.jp/agreement/"
     :association-rule/url-provenance :official-association-site
     :association-rule/retrieved-at "2026-07-14"
     :association-rule/topic #{:consumer-protection :fair-transaction}}]})

(defn spec-basis [assoc-slug] (get catalog assoc-slug))

(defn coverage
  ([] (coverage (keys catalog)))
  ([slugs]
   (let [have (filter catalog slugs)
         missing (remove catalog slugs)]
     {:requested (count slugs)
      :covered (count have)
      :covered-associations (vec (sort have))
      :missing-associations (vec (sort missing))
      :note (str "cloud-itonami-assoc-6419-jpn-zenginkyo Wave 0 (ADR-2607141700): "
                 (count (get catalog "zenginkyo")) " zenginkyo rules seeded with an "
                 "official zenginkyo.or.jp citation. Extend "
                 "`association.facts/catalog`, never fabricate a rule id/url.")})))

(defn by-topic [assoc-slug topic]
  (filterv #(contains? (:association-rule/topic %) topic) (spec-basis assoc-slug)))
