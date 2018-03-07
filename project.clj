(require '[clojure.edn :as edn])

(def +deps+ (-> "deps.edn" slurp edn/read-string))

(defn deps->vec [deps]
  (vec (map (fn [[dep {:keys [:mvn/version exclusions]}]]
              (cond-> [dep version]
                exclusions (conj :exclusions exclusions)))
            deps)))

(def dependencies
  (deps->vec (:deps +deps+)))

(def dev-dependencies
  (deps->vec (get-in +deps+ [:aliases :dev :extra-deps])))

(def source-paths
  (vec (:paths +deps+)))


(defproject powderkeg-example "0.1-dev01"
  :description "test powderkeg"
  :min-lein-version "2.0.0"
  :dependencies ~dependencies
  :pedantic? :warning
  :plugins [[duct/lein-duct "0.10.6"]
            [lein-ancient "0.6.15" :exclusions [commons-logging org.clojure/clojure commons-codec]]
            [jonase/eastwood "0.2.6-beta2"]
            [lein-kibit "0.1.6" :exclusions [org.clojure/clojure]]
            [lein-environ "1.1.0"]
            [lein-zip "0.1.1"]
            [lein-codox "0.10.3" :exclusions [org.clojure/clojure]]]
  :main powderkeg-example.core
  :source-paths ~source-paths
  ;; :repositories {"aliyun" "http://maven.aliyun.com/nexus/content/groups/public"}
  :profiles {:check {:global-vars {*warn-on-reflection* true}}
             :dev  [:project/dev :profiles/dev]
             :repl {:prep-tasks   ^:replace ["javac" "compile"]
                    :repl-options {:init-ns user}}
             :profiles/dev {}
             :project/dev  {:source-paths   ["dev/src"]
                            :resource-paths ["dev/resources"]
                            :dependencies ~dev-dependencies}
             :uberjar {:aot :all}
             }
  :jvm-opts ["-Xmx6g"]
  )
