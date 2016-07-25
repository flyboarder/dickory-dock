(ns dickory-dock.select
  (:require [clojure.string :as s]
            [clojure.zip :as zip]))

(defn at-rule
  "Return a function that takes a zip-loc argument and returns the
   zip-loc passed in if it has the given at-rule. The at-rule argument can only
   be a String. The rule name comparison is done case-insensitively."
  [tag]
  (fn [zip-loc]
    (let [node (zip/node zip-loc)
          node-tag (-> node :at-rule)]
      (when (clojure.core/and node-tag (= (s/lower-case (name node-tag)) (s/lower-case (name tag))))
        zip-loc))))

(defn url
  "Return a function that takes a zip-loc argument and returns the
   zip-loc passed in if it has a :url (@import and @namespace rules only)
   and the url passes the optional predicate."
  ([]
    (url (fn [_] true)))
  ([predicate]
   (fn [zip-loc]
     (let [node (zip/node zip-loc)
           node-tag (-> node :url)]
       (when (clojure.core/and node-tag (predicate node-tag))
         zip-loc)))))
