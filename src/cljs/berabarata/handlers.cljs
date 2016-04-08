(ns berabarata.handlers
  (:require [cljs.reader :as reader]
            [re-frame.core :refer [register-handler]]))

(def initial-state
  {:beers  {"beer0" {:id "beer0" :name "Cerveja A" :capacity 0 :price 0 :editing? false}
            "beer1" {:id "beer1" :name "Cerveja B" :capacity 0 :price 0 :editing? false}}})

(register-handler
  :initialize
  (fn [db _]
    (merge db initial-state)))

(register-handler
  :change-price
  (fn [db [_ id price]]
    (if (number? price)
      db
      (update-in db [:beers id :price] #(reader/read-string price)))))

(register-handler
  :change-capacity
  (fn [db [_ id capacity]]
    (if (number? capacity)
      db
      (update-in db [:beers id :capacity] #(reader/read-string capacity)))))

(register-handler
  :toggle-beer-editing
  (fn [db [_ id]]
    (update-in db [:beers id :editing?] not)))

