(set-env!
 :dependencies  '[[org.clojure/clojure "1.7.0"]
                  [boot/core           "2.6.0"]
                  [adzerk/bootlaces    "0.1.13" :scope "test"]
                  [degree9/boot-semver "1.2.0" :scope "test"]
                  [hickory             "0.6.0" :scope "provided"]
                  [com.helger/ph-css   "5.0.0"]]

 :resource-paths   #{"src"})

(require
 '[adzerk.bootlaces :refer :all]
 '[boot-semver.core :refer :all])

(task-options!
  pom {:project 'degree9/dickory-dock
       :version (get-version)
       :description "CSS Support for Hickory."
       :url         "https://github.com/degree9/dickory-dock"
       :scm         {:url "https://github.com/degree9/dickory-dock"}})

(deftask dev
  "Build dickory-dock for development."
  []
  (comp
   (watch)
   (version :no-update true
            :minor 'inc
            :patch 'zero
            :pre-release 'snapshot)
   (target  :dir #{"target"})
   (build-jar)))

(deftask deploy
  "Build dickory-dock and deploy to clojars."
  []
  (comp
   (version :minor 'inc
            :patch 'zero)
   (target  :dir #{"target"})
   (build-jar)
   (push-release)))
