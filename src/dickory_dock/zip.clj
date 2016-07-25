(ns dickory-dock.zip
  (:require [clojure.zip :as zip]))

(defn css-zip
  "Returns a zipper for CSS OM maps (as from as-css),
  given a root element."
  [root]
  (zip/zipper
    (complement string?)
    (comp seq :rules)
    (fn [node children]
      (assoc node :rules (and children (apply vector children))))
    root))
