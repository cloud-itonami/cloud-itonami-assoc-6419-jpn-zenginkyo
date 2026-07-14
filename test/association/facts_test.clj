(ns association.facts-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [association.facts :as facts]))

(deftest zenginkyo-has-spec-basis
  (let [sb (facts/spec-basis "zenginkyo")]
    (is (= 2 (count sb)))
    (is (every? #(str/starts-with? (:association-rule/url %) "https://www.zenginkyo.or.jp/") sb))
    (is (every? #(= "6419" (:association-rule/isic %)) sb))))

(deftest unknown-association-has-no-spec-basis
  (is (nil? (facts/spec-basis "keidanren")))
  (is (nil? (facts/spec-basis "zzz"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["zenginkyo" "keidanren"])]
    (is (= 2 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["keidanren"] (:missing-associations c)))))

(deftest by-topic-filters
  (is (= ["zenginkyo.code-of-conduct"]
         (mapv :association-rule/id (facts/by-topic "zenginkyo" :ethics))))
  (is (empty? (facts/by-topic "zenginkyo" :labor)))
  (is (empty? (facts/by-topic "keidanren" :ethics))))
