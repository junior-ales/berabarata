(ns berabarata.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub
                                   subscribe]]))

(register-sub
  :all-beers
  (fn [db]
    (reaction (vals (:beers @db)))))

(defn calc-liter-price [{:keys [price capacity]}]
  (when-not (or (zero? price)
                (zero? capacity))
    (-> price
        (* 1000.0)
        (/ capacity))))

(register-sub
  :best-beer
  (fn [db]
    (let [beers (subscribe [:all-beers])
          beers-with-liter (map
                             #(assoc % :liter-price (calc-liter-price %))
                             @beers)]
      (reaction (first (sort-by :liter-price beers-with-liter))))))

(register-sub
  :editing-beer?
  (fn [db [_ id]]
    (reaction (get-in @db [:beers id :editing?]))))

(register-sub
  :editing-new-item?
  (fn [db]
    (reaction (get @db :editing-new-item?))))
