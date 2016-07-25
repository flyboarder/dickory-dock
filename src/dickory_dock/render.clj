(ns dickory-dock.render
  (:require [clojure.string :as s]
            [hickory.utils :as utils]))

(declare css-to-stylesheet)

(defn queries->str [cssdat]
  (when (:queries cssdat) (s/join ", " (:queries cssdat))))

(defn declarations->str [cssdat]
  (s/join " " (map #(str (name (key %)) ":" (val %) ";") (:declarations cssdat))))

(defn rules->str [cssdat]
  (s/join " " (map css-to-stylesheet (:rules cssdat))))

(defn selectors->str [cssdat]
  (s/join ", " (:selectors cssdat)))

(defn css-to-stylesheet
  "Given a hickory CSS OM map structure (as returned by as-css), returns a
   string containing Stylesheet it represents. Keep in mind this function is not
   super fast or heavy-duty.
   Note that it will NOT in general be the case that
     (= my-css-src (css-to-stylesheet (as-css (parse-css my-css-src))))
   as we do not keep any letter case or whitespace information, etc."
  [cssdat]
  (if (string? cssdat)
    (utils/html-escape cssdat)
    (try
      (case (:type cssdat)
        :stylesheet (s/join " " (map css-to-stylesheet (:rules cssdat)))
        :import (str "@import url('" (:url cssdat) "') " (queries->str cssdat) ";")
        :namespace (str "@namespace " (:prefix cssdat) " url('" (:url cssdat) "');")
        :media (str "@media " (queries->str cssdat) " {" (rules->str cssdat) "}")
        :fontface (str "@font-face {" (declarations->str cssdat) "}")
        :keyframes (str "@keyframes " (:animation cssdat) " {" (rules->str cssdat) "}")
        :keyframesblock (str (:selector cssdat) " {" (declarations->str cssdat) "}")
        :viewport (str "@viewport {" (declarations->str cssdat) "}")
        :supports (str "@supports " (:condition cssdat) " {" (rules->str cssdat) "}")
        :style (str (selectors->str cssdat) " {" (declarations->str cssdat) "}")
        :unknown (str (:at-rule cssdat) " " (:parameters cssdat) " {" (rules->str cssdat) "}"))
      (catch IllegalArgumentException e
        (throw
         (if (utils/starts-with (.getMessage e) "No matching clause: ")
           (ex-info (str "Not a valid node: " (pr-str cssdat)) {:cssom cssdat})
           e))))))
