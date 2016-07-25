(ns dickory-dock.core
  (:require [dickory-dock.render :as rend])
  (:import (com.helger.css.decl CascadingStyleSheet CSSImportRule CSSNamespaceRule
                                CSSStyleRule CSSPageRule CSSMediaRule CSSFontFaceRule
                                CSSKeyframesRule CSSViewportRule CSSSupportsRule CSSUnknownRule
                                CSSSelector CSSDeclaration CSSMediaQuery CSSKeyframesBlock)
           com.helger.commons.charset.CCharset
           com.helger.css.ECSSVersion
           com.helger.css.writer.CSSWriterSettings
           com.helger.css.reader.CSSReader))

(declare as-css)

(def writer-settings (CSSWriterSettings. (. ECSSVersion CSS30) false))

(defn vec-css [this] (not-empty (into [] (map as-css this))))

(defn get-queries [this] (vec-css (.getAllMediaQueries this)))

(defn get-rules [this] (vec-css (.getAllRules this)))

(defn get-declarations [this] (not-empty (into {} (map as-css (.getAllDeclarations this)))))

(defprotocol CSSRepresentable
  "Objects that can be represented as CSS Rule node maps, similar to
   hickory.core, implement this protocol to make the conversion."

   (as-css [this]
     "Converts the node given into a hickory-css-format data structure. The
     node must have an implementation of the CSSRepresentable protocol;
     nodes created by parse-css already do."))

(extend-protocol CSSRepresentable
  CascadingStyleSheet
  (as-css [this] {:type :stylesheet
                  :rules (->> (concat (map as-css (.getAllImportRules this))
                                      (map as-css (.getAllNamespaceRules this))
                                      (map as-css (.getAllRules this)))
                              (into [])
                              not-empty)})

  CSSImportRule
  (as-css [this] {:type :import
                  :at-rule "@import"
                  :url (.getLocationString this)
                  :queries (get-queries this)})

  CSSNamespaceRule
  (as-css [this] {:type :namespace
                  :at-rule "@namespace"
                  :prefix (.getNamespacePrefix this)
                  :url (.getNamespaceURL this)})

  CSSMediaRule
  (as-css [this] {:type :media
                  :at-rule "@media"
                  :queries (get-queries this)
                  :rules (get-rules this)})

  CSSFontFaceRule
  (as-css [this] {:type :fontface
                  :at-rule "@font-face"
                  :declarations (get-declarations this)})

  CSSKeyframesRule
  (as-css [this] {:type :keyframes
                  :at-rule "@keyframes"
                  :animation (.getAnimationName this)
                  :rules (vec-css (.getAllBlocks this))})

  CSSKeyframesBlock
  (as-css [this] {:type :keyframesblock
                  :selector (.getAllKeyframesSelectors this)
                  :declarations (get-declarations this)})

  CSSViewportRule
  (as-css [this] {:type :viewport
                  :at-rule "@viewport"
                  :declarations (get-declarations this)})

  CSSSupportsRule
  (as-css [this] {:type :supports
                  :at-rule "@supports"
                  :condition (.getAllSupportConditionMembers this)
                  :rules (get-rules this)})

  CSSStyleRule
  (as-css [this] {:type :style
                  :selectors (vec-css (.getAllSelectors this))
                  :declarations (get-declarations this)})

  CSSUnknownRule
  (as-css [this] {:type :unknown
                  :at-rule (.getDeclaration this)
                  :parameters (.getParameterList this)
                  :rules (vec-css (.getBody this))})

  CSSMediaQuery
  (as-css [this] (.getAsCSSString this writer-settings 0))

  CSSSelector
  (as-css [this] (.getAsCSSString this writer-settings 0))

  CSSDeclaration
  (as-css [this] [(keyword (.getProperty this))
                  (.getAsCSSString (.getExpression this) writer-settings 0)]))

(defn parse-css [string]
  (. CSSReader (readFromString string (. CCharset CHARSET_UTF_8_OBJ) (. ECSSVersion CSS30))))

(defn parse-stylesheet [file]
  (. CSSReader (readFromFile file (. CCharset CHARSET_UTF_8_OBJ) (. ECSSVersion CSS30))))

(defn css-to-hickory [cssdat & [attrs]]
  {:type :element
   :attrs (or attrs {})
   :tag :style
   :content (rend/css-to-stylesheet cssdat)})
