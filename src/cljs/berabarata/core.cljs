(ns berabarata.core
  (:require [reagent.core          :as    r]
            [re-frame.core         :refer [dispatch-sync subscribe]]
            [berabarata.components :refer [item-list results]]))

(defn main-panel []
  (let [beers (subscribe [:all-beers])]
    [:section
     [item-list @beers]
     [results]]))

(defn ^:export init []
  (dispatch-sync [:initialize])
  (r/render [main-panel]
            (js/document.getElementById "app")))
