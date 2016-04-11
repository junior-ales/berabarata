(ns berabarata.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub
                                   subscribe]]))

(register-sub
  :all-items
  (fn [db]
    (reaction (vals (:items @db)))))

(defn calc-liter-price [{:keys [price capacity]}]
  (-> price (* 1000.0) (/ capacity)))

(defn add-liter-price [item]
  (assoc item :liter-price (calc-liter-price item)))

(defn valid-items [{:keys [price capacity]}]
  (not (or (zero? price)
           (zero? capacity))))

(register-sub
  :cheapest-item
  (fn [db]
    (let [items            (subscribe [:all-items])
          eligible-items   (filter valid-items @items)
          items-with-liter (map add-liter-price eligible-items)]
      (reaction (first
                  (sort-by :liter-price items-with-liter))))))

(register-sub
  :editing-new-item?
  (fn [db]
    (reaction (get @db :editing-new-item?))))
