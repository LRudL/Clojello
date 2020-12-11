(defproject clojello "0.2.0"
  :description "Generalised implementation of Othello."
  :url "https://github.com/LRudL/Clojello"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [proto-repl "0.3.1"]]
  :main ^:skip-aot clojello.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
