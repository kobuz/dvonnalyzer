(defproject dvonnalyzer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.2.0"]
                 [ring/ring-defaults "0.1.2"]
                 [selmer "0.7.2"]
                 [clj-http-lite "0.2.0"]
                 [org.clojure/clojurescript "0.0-2371"]]
  :plugins [[lein-ring "0.8.13"]
            [lein-cljsbuild "1.0.3"]]
  :cljsbuild {
    :builds [{
        :source-paths ["src/cljs"]
        :compiler {
          :output-to "resources/public/js/app.js"
          :optimizations :whitespace
          :pretty-print true}}]}
  :ring {:handler dvonnalyzer.core.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
