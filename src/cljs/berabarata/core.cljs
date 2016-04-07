(ns berabarata.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core  :as    r]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [cljs.reader :as reader]))

(def initial-state
  {:beers  {"beer0" {:id "beer0" :name "Cerveja A" :capacity 0 :price 0}
            "beer1" {:id "beer1" :name "Cerveja B" :capacity 0 :price 0}}})

(register-handler
  :initialize
  (fn [db _]
    (merge db initial-state)))

(register-handler
  :change-price
  (fn [db [_ id price]]
    (update-in db [:beers id :price] #(reader/read-string price))))

(register-handler
  :change-capacity
  (fn [db [_ id capacity]]
    (update-in db [:beers id :capacity] #(reader/read-string capacity))))

(defn calc-liter-price [{:keys [price capacity]}]
  (when-not (or (zero? price)
                (zero? capacity))
    (-> price
        (* 1000.0)
        (/ capacity))))

(register-sub
  :all-beers
  (fn [db]
    (reaction (vals (:beers @db)))))

(register-sub
  :best-beer
  (fn [db]
    (let [beers (subscribe [:all-beers])
          beers-with-liter (map
                             #(assoc % :liter-price (calc-liter-price %))
                             @beers)]
      (reaction (first (sort-by :liter-price beers-with-liter))))))

(defn title []
  [:header
   [:h1 "Bera Barata"]
   [:p "Calculadora para comprar mais gastando menos"]])

(defn results []
  (let [best-beer (subscribe [:best-beer])]
    [:section
     [:h3 "Cerveja Mais Em Conta"]
     [:p "Nome: " (:name @best-beer)]
     [:p "Tamanho: " (:capacity @best-beer)]
     [:p "Preço: " (:price @best-beer)]
     [:p "Preço por litro: " (:liter-price @best-beer)]]))

(defn make-beer [beer]
  (let [form-price (r/atom 0)
        form-capacity (r/atom 0)]
    [:article {:key (:id beer)}
     [:h4>em (:name beer)]
     [:div.beer-item
      [:label "R$"]
      [:input {:type "number"
               :step "0.01"
               :on-change #(reset! form-price (-> % .-target .-value))
               :on-blur #(dispatch
                           [:change-price (:id beer) @form-price])}]
      [:label "ml"]
      [:input {:type "number"
               :step "1"
               :on-change #(reset! form-capacity (-> % .-target .-value))
               :on-blur #(dispatch
                           [:change-capacity (:id beer) @form-capacity])}]]]))

(defn main-panel []
  (let [beers (subscribe [:all-beers])]
    [:section
     [title]
     (map make-beer @beers)
     [results]]))

(defn ^:export init []
  (dispatch-sync [:initialize])
  (r/render [main-panel]
            (js/document.getElementById "app")))
