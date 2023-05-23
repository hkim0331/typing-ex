(defproject typing-ex "1.18.9"
  :description "typing exercises for literacy classes"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[buddy/buddy-auth "3.0.323"]
                 [buddy/buddy-hashers "1.8.158"]
                 [cheshire/cheshire "5.11.0"]
                 [clojure.java-time "1.2.0"]
                 [com.github.seancorfield/next.jdbc "1.3.874"]
                 #_[com.taoensso/timbre "6.1.0"]
                 [duct/core "0.8.0"]
                 [duct/module.ataraxy "0.3.0"]
                 [duct/module.logging "0.5.0"]
                 [duct/module.sql "0.6.1"]
                 [duct/module.web "0.7.3"]
                 [environ "1.2.0"]
                 [hato "0.9.0"]
                 [hiccup "1.0.5"]
                 ;; develop only?
                 ;; [integrant/repl "0.3.2"]
                 ;;
                 [org.clojure/clojure "1.11.1"]
                 [org.postgresql/postgresql "42.6.0"]]
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
                  :dependencies   [[integrant/repl "0.3.2"]
                                   [hawk "0.2.11"]
                                   [eftest "0.6.0"]
                                   [fipp "0.6.26"]
                                   [kerodon "0.9.1"]]}})
