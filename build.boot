(set-env!
 :source-paths #{"src/clj"}
 :dependencies '[[org.clojure/clojure "1.9.0-alpha17" :scope "provided"]
                 [com.datomic/clj-client "0.8.606"]
                 [org.clojure/core.async "0.3.443"]
                 [com.datomic/datomic-free "0.9.5561.50"]])

(require 'boot.repl)

(swap! boot.repl/*default-dependencies*
       concat '[[cider/cider-nrepl "0.15.0-SNAPSHOT" :scope "test"]])

(swap! boot.repl/*default-middleware*
       conj 'cider.nrepl/cider-middleware)


(deftask dev
  []
  (comp
   (watch)
   (repl :server true
         :port   3311)
   (target :dir #{"target"})))


(deftask build
  []
  (comp
   ;;(aot :namespace #{'ds.core})
   (uber)
   (jar :file "app.jar" :main 'ds.core)
   (sift :include #{#"app.jar"})
   (target :dir #{"target"})))


