(ns association-facts-test
  (:require [clojure.java.io :as io] [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is testing]]
            [kotoba.compiler.core :as compiler] [kotoba.kir :as ir]))
(def source (slurp "src/association_facts.kotoba"))
(defn call [kir f & xs] (ir/execute kir f (vec xs)))
(defn present [x] (when (second x) (nth x 2)))
(def fields ["id" "title" "association" "isic" "country" "kind" "url" "url-provenance"
             "established-date" "last-revised-date" "retrieved-at"
             "addressee" "parameter-disposition"])
(def expected
  [{"id" "zenginkyo.code-of-conduct" "title" "Code of Conduct (倫理憲章)"
    "association" "zenginkyo" "isic" "6419" "country" "JPN" "kind" "self-regulatory-code"
    "url" "https://www.zenginkyo.or.jp/en/conduct/"
    "url-provenance" "official-association-site" "established-date" "2005-11-22"
    "last-revised-date" "2022-09-15" "retrieved-at" "2026-07-14"
    "addressee" "member-bank" "parameter-disposition" "no-parameter"}
   {"id" "zenginkyo.agreements" "title" "申し合わせ (Board-resolved Member-Bank Arrangements)"
    "association" "zenginkyo" "isic" "6419" "country" "JPN" "kind" "self-regulatory-code"
    "url" "https://www.zenginkyo.or.jp/agreement/"
    "url-provenance" "official-association-site" "established-date" nil
    "last-revised-date" nil "retrieved-at" "2026-07-14"
    "addressee" "member-bank" "parameter-disposition" "no-parameter"}
   {"id" "zenginkyo.corporate-ib-unauthorized-withdrawal-response"
    "title" "法人向けインターネット・バンキングに係る預金等の不正な払戻しへの対応について"
    "association" "zenginkyo" "isic" "6419" "country" "JPN" "kind" "self-regulatory-code"
    "url" "https://www.zenginkyo.or.jp/news/2014/n3344/"
    "url-provenance" "official-association-site" "established-date" "2014-05-15"
    "last-revised-date" nil "retrieved-at" "2026-07-29"
    "addressee" "member-bank" "parameter-disposition" "delegates-to-adopter"}
   {"id" "zenginkyo.corporate-ib-compensation"
    "title" "法人向けインターネット・バンキングにおける預金等の不正な払戻しに関する補償の考え方について"
    "association" "zenginkyo" "isic" "6419" "country" "JPN" "kind" "self-regulatory-code"
    "url" "https://www.zenginkyo.or.jp/news/2014/n3349/"
    "url-provenance" "official-association-site" "established-date" "2014-07-17"
    "last-revised-date" nil "retrieved-at" "2026-07-29"
    "addressee" "member-bank" "parameter-disposition" "conditions-liability-on-adopter-choice"}
   {"id" "zenginkyo.financial-crime-response"
    "title" "金融犯罪への対応の徹底に係る申し合わせについて"
    "association" "zenginkyo" "isic" "6419" "country" "JPN" "kind" "self-regulatory-code"
    "url" "https://www.zenginkyo.or.jp/news/2024/n051001/"
    "url-provenance" "official-association-site" "established-date" "2024-05-10"
    "last-revised-date" nil "retrieved-at" "2026-07-29"
    "addressee" "member-bank" "parameter-disposition" "no-parameter"}
   {"id" "zenginkyo.corporate-ib-fraud-alert"
    "title" "法人向けインターネット・バンキングにおける不正送金にご注意！"
    "association" "zenginkyo" "isic" "6419" "country" "JPN" "kind" "public-guidance"
    "url" "https://www.zenginkyo.or.jp/topic/detail/nid/3563/"
    "url-provenance" "official-association-site" "established-date" nil
    "last-revised-date" nil "retrieved-at" "2026-07-29"
    "addressee" "corporate-customer" "parameter-disposition" "delegates-to-adopter"}])
(def expected-topics
  [["ethics" "member-conduct"]
   ["consumer-protection" "fair-transaction"]
   ["corporate-internet-banking" "transfer-limit"]
   ["corporate-internet-banking" "compensation"]
   ["financial-crime" "member-conduct"]
   ["corporate-internet-banking" "transfer-limit"]])
(def dispositions
  ["sets-numeric-default" "delegates-to-adopter"
   "conditions-liability-on-adopter-choice" "no-parameter"])
(def indices (vec (range (count expected))))
(def all-topics (distinct (apply concat expected-topics)))

(deftest reference-preserves-authority
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))
        observed (mapv (fn [i] (into {} (map (fn [f] [f (present (call kir 'entry-field "zenginkyo" i f))]) fields))) indices)]
    (is (= 6 (call kir 'entry-count "zenginkyo")))
    (is (= expected observed))
    (is (= expected-topics
           (mapv (fn [i] (mapv #(present (call kir 'topic "zenginkyo" i %)) (range (call kir 'topic-count "zenginkyo" i)))) indices)))
    (is (= "zenginkyo.code-of-conduct" (present (call kir 'by-topic-id "zenginkyo" "member-conduct" 0))))
    (is (= "zenginkyo.agreements" (present (call kir 'by-topic-id "zenginkyo" "fair-transaction" 0))))
    (is (= #{} (set (:effects kir))))
    (testing "fail closed"
      (is (zero? (call kir 'entry-count "japanese-bankers-association")))
      (is (zero? (call kir 'entry-count "keidanren")))
      (is (nil? (present (call kir 'entry-field "zenginkyo" 6 "id"))))
      (is (nil? (present (call kir 'entry-field "zenginkyo" 1 "last-revised-date"))))
      (is (nil? (present (call kir 'topic "zenginkyo" 0 2))))
      (is (zero? (call kir 'by-topic-count "zenginkyo" "labor")))
      (is (nil? (present (call kir 'by-topic-id "zenginkyo" "ethics" 1)))))))

(deftest counts-are-derived-not-restated
  ;; `by-topic-count` / `disposition-count` scan the same tables `entry-field`
  ;; and `topic` read, so an entry cannot be added without the counts moving
  ;; with it. These assertions fail if a count is ever restated as a literal
  ;; that drifts from the table it claims to summarize.
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))]
    (is (= (reduce + (map count expected-topics))
           (reduce + (map #(call kir 'by-topic-count "zenginkyo" %) all-topics)))
        "every topic membership is counted exactly once")
    (doseq [t all-topics]
      (is (= (count (filter #(some #{t} %) expected-topics))
             (call kir 'by-topic-count "zenginkyo" t))
          (str "by-topic-count " t))
      (is (= (call kir 'by-topic-count "zenginkyo" t)
             (reduce + (map #(call kir 'disposition-count "zenginkyo" t %) dispositions)))
          (str "every entry under topic " t " carries a disposition from the closed set")))))

(deftest association-names-the-transfer-limit-but-sets-no-number
  ;; The catalog's own record of what the association has published about the
  ;; corporate net-banking transfer limit. Two documents raise the parameter
  ;; (2014-05-15 to member banks, and the undated customer-facing alert) and
  ;; BOTH hand the number to whoever adopts the rule. A third ties compensation
  ;; to whether the adopter chose well. None fixes a number.
  ;;
  ;; This is an absence in the published record, not a recommendation: if
  ;; zenginkyo later sets a default, adding that entry moves this count off
  ;; zero and this test is the thing that must be updated to say so.
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))]
    (is (= 2 (call kir 'by-topic-count "zenginkyo" "transfer-limit")))
    (is (= 0 (call kir 'sets-numeric-default-count "zenginkyo" "transfer-limit")))
    (is (= 2 (call kir 'disposition-count "zenginkyo" "transfer-limit" "delegates-to-adopter")))
    (is (= 1 (call kir 'disposition-count "zenginkyo" "corporate-internet-banking"
                      "conditions-liability-on-adopter-choice")))
    (is (= ["zenginkyo.corporate-ib-unauthorized-withdrawal-response"
            "zenginkyo.corporate-ib-fraud-alert"
            nil]
           (mapv #(present (call kir 'by-topic-id "zenginkyo" "transfer-limit" %)) [0 1 2])))
    (testing "who was handed the number is always recorded"
      (is (= ["member-bank" "corporate-customer"]
             (mapv (fn [i] (present (call kir 'entry-field "zenginkyo" i "addressee")))
                   (keep-indexed #(when (some #{"transfer-limit"} %2) %1) expected-topics)))))
    (testing "no fabricated numeric default under any catalog topic"
      (doseq [t all-topics]
        (is (= 0 (call kir 'sets-numeric-default-count "zenginkyo" t))
            (str t " -- if this fails, an entry claims a numeric default; verify it against the live document"))))))

(deftest datascript-tx-matches-kotoba-authority
  ;; `data/datascript-tx.edn` is documented as DERIVED from the Kotoba
  ;; catalog. Nothing regenerates it, so without this check the two could
  ;; drift silently and a downstream DataScript query would answer from a
  ;; stale copy while the tests passed against the real authority.
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))
        tx (read-string (slurp "data/datascript-tx.edn"))
        kw #(when % (keyword %))
        from-kotoba
        (mapv (fn [i]
                (let [f #(present (call kir 'entry-field "zenginkyo" i %))]
                  (cond-> {:association-rule/id (f "id")
                           :association-rule/title (f "title")
                           :association-rule/association (f "association")
                           :association-rule/isic (f "isic")
                           :association-rule/country (f "country")
                           :association-rule/kind (kw (f "kind"))
                           :association-rule/url (f "url")
                           :association-rule/url-provenance (kw (f "url-provenance"))
                           :association-rule/retrieved-at (f "retrieved-at")
                           :association-rule/addressee (kw (f "addressee"))
                           :association-rule/parameter-disposition (kw (f "parameter-disposition"))
                           :association-rule/topic (mapv #(keyword (present (call kir 'topic "zenginkyo" i %)))
                                                         (range (call kir 'topic-count "zenginkyo" i)))}
                    (f "established-date") (assoc :association-rule/established-date (f "established-date"))
                    (f "last-revised-date") (assoc :association-rule/last-revised-date (f "last-revised-date")))))
              indices)]
    (is (= (count from-kotoba) (count tx)))
    (is (= from-kotoba (mapv #(into {} %) tx)))))

(defn compiler-root [] (nth (iterate #(.getParent ^java.nio.file.Path %)
  (java.nio.file.Path/of (.toURI (io/resource "kotoba/compiler/core.clj")))) 4))
(defn base64 [x] (.encodeToString (java.util.Base64/getEncoder) x))
(deftest restricted-js-and-wasm-conform-semantically
  (let [js (compiler/compile-source source :js-kotoba-v1) wasm (compiler/compile-source source :wasm32-browser-kotoba-v1)
        js64 (base64 (.getBytes ^String (:source js) "UTF-8")) wasm64 (base64 ^bytes (:bytes wasm))
        p (shell/sh "node" "--input-type=module" "-e"
            (str "import(process.argv[1]).then(async h=>{const j=await import('data:text/javascript;base64," js64 "');const w=await h.instantiateKotoba(Buffer.from(process.argv[2],'base64'));const r=x=>{if(x['entry-field']('zenginkyo',0n,'established-date')[2]!=='2005-11-22'||x['entry-field']('zenginkyo',0n,'last-revised-date')[2]!=='2022-09-15'||x['entry-field']('zenginkyo',1n,'last-revised-date')[1]!==false)throw Error('dates');if(x['by-topic-id']('zenginkyo','fair-transaction',0n)[2]!=='zenginkyo.agreements'||x['entry-count']('japanese-bankers-association')!==0n||x['entry-count']('keidanren')!==0n)throw Error('authority');if(x['entry-count']('zenginkyo')!==6n)throw Error('count');if(x['sets-numeric-default-count']('zenginkyo','transfer-limit')!==0n||x['disposition-count']('zenginkyo','transfer-limit','delegates-to-adopter')!==2n||x['by-topic-count']('zenginkyo','corporate-internet-banking')!==3n)throw Error('disposition');if(x['entry-field']('zenginkyo',5n,'addressee')[2]!=='corporate-customer'||x['entry-field']('zenginkyo',5n,'established-date')[1]!==false)throw Error('alert');};r(j.instantiateKotoba({}));r(w.instance.exports)}).catch(e=>{console.error(e);process.exit(99)})")
            (.toString (.toUri (.resolve (compiler-root) "runtime/browser-host.mjs"))) wasm64)]
    (is (zero? (:exit p)) (str (:out p) (:err p)))))
(deftest production-source-authority
  (is (= ["src/association_facts.kotoba"] (->> (file-seq (io/file "src")) (filter #(.isFile %)) (map str) sort vec))))
