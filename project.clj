(defproject dvonnalyzer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.2.0"]
                 [ring/ring-defaults "0.1.2"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [selmer "0.8.2"]
                 [clj-http-lite "0.2.0"]
                 [org.clojure/clojurescript "0.0-3211"]
                 [reagent "0.5.0"]
                 [cljs-ajax "0.3.13"]]
  :plugins [[lein-ring "0.9.6"]
            [lein-cljsbuild "1.0.6"]
            [lein-figwheel "0.3.7"]]
  :figwheel {:http-server-root "public"
             :server-port 3449
             :ring-handler dvonnalyzer.handler/app}
  :clean-targets ^{:protect false} [:target-path "out"]
  :cljsbuild {
    :builds {:app {
               :source-paths ["src/cljs"]
               :figwheel true
               :compiler {
                 :main dvonnalyzer.core
                 :output-to "resources/public/js/app.js"
                 :output-dir "resources/public/js/out"
                 :externs ["react/externs/react.js"]
                 :asset-path "/js/out"
                 :pretty-print true}}}}
  :ring {:handler dvonnalyzer.handler/app}
  :uberjar-name "dvonnalyzer-standalone.jar"
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}
             :uberjar {:main dvonnalyzer.core
                       :aot :all
                       :hooks [leiningen.cljsbuild]
                       :cljsbuild {
                         :jar true
                         :builds {:app {:compiler {:optimizations :advanced
                                                   :pretty-print false}}}}}})
