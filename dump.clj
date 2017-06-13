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
                    :movie/release-year 1984}])


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
