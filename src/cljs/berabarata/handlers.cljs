(ns berabarata.handlers
  (:require [cljs.reader :as reader]
            [re-frame.core :refer [register-handler]]))

(def initial-state
  {:beers {} :editing-new-item? false})

(register-handler
  :initialize
  (fn [db _]
    (merge db initial-state)))

(register-handler
  :change-name
  (fn [db [_ id name]]
    (if (empty? (clojure.string/trim (or name "")))
      db
      (update-in db [:beers id :name] #(str name)))))

(register-handler
  :change-brand
  (fn [db [_ id brand]]
    (if (empty? (clojure.string/trim (or brand "")))
      db
      (update-in db [:beers id :brand] #(str brand)))))

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
  :toggle-item-editing
  (fn [db [_ id]]
    (update-in db [:beers id :editing?] not)))

(register-handler
  :edit-new-item
  (fn [db]
    (update-in db [:editing-new-item?] not)))

(defn make-item [id name]
  {:id id
   :name name
   :brand nil
   :capacity 0
   :price 0
   :enabled? true
   :editing? false})

(register-handler
  :create-new-item
  (fn [db [_ name]]
    (let [item-id (str "item-" (inc (count (:beers db))))]
      (-> db
          (assoc-in [:beers item-id] (make-item item-id name))
          (update-in [:editing-new-item?] not)))))

(register-handler
  :toggle-item-state
  (fn [db [_ id]]
    (update-in db [:beers id :enabled?] not)))
