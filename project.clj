(defproject typing-ex "v2.18-snapshot-2"
  :description "typing exercises for literacy classes"
  :url "https://tp.melt.kyutech.ac.jp"
  :min-lein-version "2.0.0"
  :dependencies [[buddy/buddy-auth "3.0.323"]
                 [buddy/buddy-hashers "2.0.167"]
                 [cheshire/cheshire "5.13.0"]
                 [clojure.java-time/clojure.java-time "1.4.2"]
                 [com.github.seancorfield/next.jdbc "1.3.939"]
                 [duct/core "0.8.1"]
                 [duct/module.ataraxy "0.3.0"]
                 [duct/module.logging "0.5.0"]
                 [duct/module.sql "0.6.1"]
                 [duct/module.web "0.7.3"]
                 [environ "1.2.0"]
                 [hato "1.0.0"]
                 [hiccup "1.0.5"]
                 [com.taoensso/carmine "3.4.1"]
                 [org.clojure/clojure "1.12.0"]
                 [org.postgresql/postgresql "42.7.4"]]
  :plugins [[duct/lein-duct "0.12.3"]]
  :main ^:skip-aot typing-ex.main
  :resource-paths ["resources" "target/resources"]
  :prep-tasks     ["javac" "compile" ["run" ":duct/compiler"]]
  :middleware     [lein-duct.plugin/middleware]
  :profiles
  {:dev  [:project/dev :profiles/dev]
   :repl {:prep-tasks   ^:replace ["javac" "compile"]
          :repl-options {:init-ns user}}
   :uberjar {:aot :all}
   :profiles/dev {}
   :project/dev  {:source-paths   ["dev/src"]
                  :resource-paths ["dev/resources"]
                  :dependencies   [[integrant/repl "0.3.3"]
                                   [hawk "0.2.11"]
                                   [eftest "0.6.0"]
                                   [fipp "0.6.26"]
                                   [kerodon "0.9.1"]]}})
