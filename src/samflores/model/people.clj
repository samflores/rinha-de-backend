(ns samflores.model.people
  (:require
   [clojure.set :refer [map-invert rename-keys]]
   [clojure.spec.alpha :as s]
   [clojure.string :refer [trim]]
   [samflores.db.people :refer [all-people insert-person person-by-id
                                search-for]]
   [samflores.db.people-stacks :refer [insert-person-stack]]
   [samflores.db.stacks :refer [insert-stacks]]))

(def ^:private date-regex #"^\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$")

(s/def :person/name (s/and string? #(<= 1 (-> % trim count) 100)))
(s/def :person/nickname (s/and string? #(<= 1 (-> % trim count) 32)))
(s/def :person/born_on (s/and string? #(re-matches date-regex %)))
(s/def :person/stacks (s/and vector? #(every? string? %)))
(s/def :model/person (s/keys :req-un [:person/name :person/nickname :person/born_on]
                             :opt-un [:person/stacks]))

(def ^:private presentation-keys
  {:name :nome
   :nickname :apelido
   :born_on :nascimento})

(def ^:private database-keys
  (map-invert presentation-keys))

(defn- update-stacks [person]
  (update person :stacks #(->> (if % (.getArray %) []) seq (filter identity))))

(defn- update-born-on [person]
  (update person :born_on #(.toString %)))

(defn- format-to-presentation [person]
  (rename-keys person presentation-keys))

(defn- format-to-validation [person]
  (rename-keys person database-keys))

(defn create [db data]
  (let [data (format-to-validation data)]
    (if (s/valid? :model/person data)
      (let [stack-data (:stacks data)
            person-data (dissoc data :stacks)
            person-id (->> person-data (insert-person db) first :id)
            stack-ids (if (empty? stack-data) [] (map :id (insert-stacks db {:names (map vector stack-data)})))
            id-pairs  (map (partial vector person-id) stack-ids)
            _people-stacks (if (empty? stack-ids) [] (insert-person-stack db {:cols id-pairs}))]
        {:id person-id})
      {:error (s/explain-str :model/person data)})))

(defn find [db person-id]
  (format-to-presentation (update-stacks (person-by-id db {:id person-id}))))

(defn search [db term]
  (->> (search-for db {:term (str "%" term "%")})
       (map (comp format-to-presentation update-born-on update-stacks))))
