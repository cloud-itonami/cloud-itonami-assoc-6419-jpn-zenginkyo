(ns association-facts-test
  (:require [clojure.java.io :as io] [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is testing]]
            [kotoba.compiler.core :as compiler] [kotoba.compiler.ir :as ir]))
(def source (slurp "src/association_facts.kotoba"))
(defn call [kir f & xs] (ir/execute kir f (vec xs)))
(defn present [x] (when (second x) (nth x 2)))
(def fields ["id" "title" "association" "isic" "country" "kind" "url" "url-provenance"
             "established-date" "last-revised-date" "retrieved-at"])
(def expected
  [{"id" "zenginkyo.code-of-conduct" "title" "Code of Conduct (倫理憲章)"
    "association" "zenginkyo" "isic" "6419" "country" "JPN" "kind" "self-regulatory-code"
    "url" "https://www.zenginkyo.or.jp/en/conduct/"
    "url-provenance" "official-association-site" "established-date" "2005-11-22"
    "last-revised-date" "2022-09-15" "retrieved-at" "2026-07-14"}
   {"id" "zenginkyo.agreements" "title" "申し合わせ (Board-resolved Member-Bank Arrangements)"
    "association" "zenginkyo" "isic" "6419" "country" "JPN" "kind" "self-regulatory-code"
    "url" "https://www.zenginkyo.or.jp/agreement/"
    "url-provenance" "official-association-site" "established-date" nil
    "last-revised-date" nil "retrieved-at" "2026-07-14"}])
(deftest reference-preserves-authority
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))
        observed (mapv (fn [i] (into {} (map (fn [f] [f (present (call kir 'entry-field "zenginkyo" i f))]) fields))) [0 1])]
    (is (= expected observed))
    (is (= [["2005-11-22" "2022-09-15"] [nil nil]]
           (mapv (fn [i] (mapv #(present (call kir 'entry-field "zenginkyo" i %)) ["established-date" "last-revised-date"])) [0 1])))
    (is (= [["ethics" "member-conduct"] ["consumer-protection" "fair-transaction"]]
           (mapv (fn [i] (mapv #(present (call kir 'topic "zenginkyo" i %)) (range (call kir 'topic-count "zenginkyo" i)))) [0 1])))
    (is (= "zenginkyo.code-of-conduct" (present (call kir 'by-topic-id "zenginkyo" "member-conduct" 0))))
    (is (= "zenginkyo.agreements" (present (call kir 'by-topic-id "zenginkyo" "fair-transaction" 0))))
    (is (= #{} (set (:effects kir))))
    (testing "fail closed"
      (is (zero? (call kir 'entry-count "japanese-bankers-association")))
      (is (zero? (call kir 'entry-count "keidanren")))
      (is (nil? (present (call kir 'entry-field "zenginkyo" 2 "id"))))
      (is (nil? (present (call kir 'entry-field "zenginkyo" 1 "last-revised-date"))))
      (is (nil? (present (call kir 'topic "zenginkyo" 0 2))))
      (is (zero? (call kir 'by-topic-count "zenginkyo" "labor")))
      (is (nil? (present (call kir 'by-topic-id "zenginkyo" "ethics" 1)))))))
(defn compiler-root [] (nth (iterate #(.getParent ^java.nio.file.Path %)
  (java.nio.file.Path/of (.toURI (io/resource "kotoba/compiler/core.clj")))) 4))
(defn base64 [x] (.encodeToString (java.util.Base64/getEncoder) x))
(deftest restricted-js-and-wasm-conform-semantically
  (let [js (compiler/compile-source source :js-kotoba-v1) wasm (compiler/compile-source source :wasm32-browser-kotoba-v1)
        js64 (base64 (.getBytes ^String (:source js) "UTF-8")) wasm64 (base64 ^bytes (:bytes wasm))
        p (shell/sh "node" "--input-type=module" "-e"
            (str "import(process.argv[1]).then(async h=>{const j=await import('data:text/javascript;base64," js64 "');const w=await h.instantiateKotoba(Buffer.from(process.argv[2],'base64'));const r=x=>{if(x['entry-field']('zenginkyo',0n,'established-date')[2]!=='2005-11-22'||x['entry-field']('zenginkyo',0n,'last-revised-date')[2]!=='2022-09-15'||x['entry-field']('zenginkyo',1n,'last-revised-date')[1]!==false)throw Error('dates');if(x['by-topic-id']('zenginkyo','fair-transaction',0n)[2]!=='zenginkyo.agreements'||x['entry-count']('japanese-bankers-association')!==0n||x['entry-count']('keidanren')!==0n)throw Error('authority');};r(j.instantiateKotoba({}));r(w.instance.exports)}).catch(e=>{console.error(e);process.exit(99)})")
            (.toString (.toUri (.resolve (compiler-root) "runtime/browser-host.mjs"))) wasm64)]
    (is (zero? (:exit p)) (str (:out p) (:err p)))))
(deftest production-source-authority
  (is (= ["src/association_facts.kotoba"] (->> (file-seq (io/file "src")) (filter #(.isFile %)) (map str) sort vec))))
