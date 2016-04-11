(ns berabarata.core
  (:require [reagent.core          :as    r]
            [re-frame.core         :refer [dispatch-sync subscribe]]
            [berabarata.components :refer [item-list results]]))

(defn main-panel []
  (let [items (subscribe [:all-items])]
    [:section
     [item-list @items]
     [results]]))

(defn ^:export init []
  (dispatch-sync [:initialize])
  (r/render [main-panel]
            (js/document.getElementById "app")))
