(ns datomic-study.core
  (:require [clojure.core.async :refer [<!!]]
            [datomic.client     :as    client]
            [datomic.api        :as    d]))


;; tutorial

(def conn
     (<!! (client/connect
           {:db-name "hello"
            :account-id client/PRO_ACCOUNT
            :secret "admin"
            :region "none"
            :endpoint "localhost:8998"
            :service "peer-server"
            :access-key "admin"})))


(defn make-idents [xs]
  (mapv #(hash-map :db/ident %) xs))

(def sizes  [:small :medium :large :xlarge])
(def types  [:shirt :pants :dress :hat])
(def colors [:red :blue :green :yellow])

(<!! (client/transact conn {:tx-data (make-idents types)}))
(<!! (client/transact conn {:tx-data (make-idents sizes)}))
(<!! (client/transact conn {:tx-data (make-idents colors)}))

(def schema-1
  [{:db/ident :inv/sku
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}

   {:db/ident :inv/color
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}

   {:db/ident :inv/size
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}

   {:db/ident :inv/type
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}])

(<!! (client/transact conn {:tx-data schema-1}))


(def sample-data
  (->> (for [color colors type types size sizes]
         {:inv/color color
          :inv/type  type
          :inv/size  size})
       (map-indexed
        (fn [idx m]
          (assoc m :inv/sku (str "SKU-" idx))))
       vec))

(<!! (client/transact conn {:tx-data sample-data}))

(def db (client/db conn))


(<!! (client/q conn {:args [db]
                     :query '[:find ?sku
                              :where
                              [?e :inv/sku "SKU-42"]
                              [?e :inv/color ?color]
                              [?e2 :inv/color ?color]
                              [?e2 :inv/sku ?sku]]}))


(def order-schema
  [{:db/ident :order/items
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/isComponent true}
   
   {:db/ident :item/id
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}

   {:db/ident :item/count
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one}])

(<!! (client/transact conn {:tx-data order-schema}))


(def add-order
  {:order/items [{:item/id [:inv/sku "SKU-25"]
                  :item/count 20}
                 {:item/id [:inv/sku "SKU-40"]
                  :item/count 10}]})

(<!! (client/transact conn {:tx-data [add-order]}))

(def db (client/db conn))


(<!! (client/q conn {:query '[:find ?sku
                              :in $ ?inv
                              :where
                              [?item :item/id ?inv]
                              [?order :order/items ?item]
                              [?order :order/items ?other-item]
                              [?other-item :item/id ?other-inv]
                              [?other-inv :inv/sku ?sku]]
                     :args [db [:inv/sku "SKU-25"]]}))







#_(
   ;;coversations
   (def uri "datomic:mem://first-conversation")

   (d/create-database uri)

   (def conn (d/connect uri))

   (def dog-schema [{:db/ident :dog/name
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one
                     :db/doc "Dog name"}

                    {:db/ident :dog/breed
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one
                     :db/doc "Dog breed"}])

   (d/transact conn dog-schema)








   ;; bin/run -m datomic.peer-server -h localhost -p 8998 -a admin,admin -d hello,datomic:mem://hello

   ;; create db connection
   (def conn
     (<!! (client/connect
           {:db-name "hello"
            :account-id client/PRO_ACCOUNT
            :secret "admin"
            :region "none"
            :endpoint "localhost:8998"
            :service "peer-server"
            :access-key "admin"})))



   ;; adding schema
   (def movie-schema [{:db/ident :movie/title
                       :db/valueType :db.type/string
                       :db/cardinality :db.cardinality/one
                       :db/doc "The title of the movie"}

                      {:db/ident :movie/genre
                       :db/valueType :db.type/string
                       :db/cardinality :db.cardinality/one
                       :db/doc "The genre of the movie"}

                      {:db/ident :movie/release-year
                       :db/valueType :db.type/long
                       :db/cardinality :db.cardinality/one
                       :db/doc "The year the movie was released in theaters"}])


   (<!! (client/transact conn {:tx-data movie-schema}))


   ;; adding data
   (def first-movies [{:movie/title "The Goonies"
                       :movie/genre "action/adventure"
                       :movie/release-year 1985}
                      {:movie/title "Commando"
                       :movie/genre "action/adventure"
                       :movie/release-year 1985}
                      {:movie/title "Repo Man"
                       :movie/genre "punk dystopia"
                       :movie/release-year 1984}]))

          #_(
             (<!! (client/transact conn {:tx-data first-movies}))




             (def db (client/db conn))




             (def all-movies-q '[:find ?e 
                                 :where [?e :movie/title]])

             (<!! (client/q conn {:query all-movies-q :args [db]}))




             (def all-titles-q '[:find ?movie-title
                                 :where [_ :movie/title ?movie-title]])

             (<!! (client/q conn {:query all-titles-q :args [db]}))



             (def titles-from-1985 '[:find ?title
                                     :where
                                     [?e :movie/title        ?title]
                                     [?e :movie/release-year 1985]])


             (<!! (client/q conn {:query titles-from-1985 :args [db]}))




             (def titles-from-1985-full '[:find ?title ?year ?genre
                                          :where
                                          [?e :movie/title ?title]
                                          [?e :movie/release-year ?year]
                                          [?e :movie/genre ?genre]
                                          [?e :movie/release-year 1985]]))

          #_((<!! (client/q conn {:query titles-from-1985-full :args [db]}))




             (def commando (ffirst (<!! (client/q conn {:query '[:find ?e
                                                                 :where [?e :movie/title "Commando"]]
                                                        :args [db]}))))

             (<!! (client/transact conn {:tx-data [{:db/id commando :movie/genre "future sci/fi"}]}))

             db

             (def old-db (client/as-of db 1004))


             (def hdb (client/history db))

             (<!! (client/q conn {:query '[:find ?genre
                                           :where
                                           [?e :movie/title "Commando"]
                                           [?e :movie/genre ?genre]]
                                  :args [hdb]})))






