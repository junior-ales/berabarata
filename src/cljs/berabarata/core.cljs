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

(register-sub
  :editing-beer?
  (fn [db [_ id]]
    (reaction (get-in @db [:beers id :editing?]))))

(defn title []
  [:header
   [:p "Calculadora para comprar mais gastando menos"]])

(defn results []
  (let [best-beer (subscribe [:best-beer])]
    [:section
     [:h3 "Cerveja Mais Em Conta"]
     [:p "Nome: " (:name @best-beer)]
     [:p "Tamanho: " (:capacity @best-beer)]
     [:p "Preço: " (:price @best-beer)]
     [:p "Preço por litro: " (:liter-price @best-beer)]]))

(defn beer-item [{:keys [id name price capacity]}]
  (let [form-price (r/atom 0)
        form-capacity (r/atom 0)
        price-format (str "R$ " (.toFixed price 2))
        capacity-format (str capacity "ml")
        editing? (subscribe [:editing-beer? id])]
    [:li {:key (str name "-" capacity "-item")}
     [:div.mdl-list__item.mdl-list__item--two-line
      [:span.mdl-list__item-primary-content
       [:i.material-icons.mdl-list__item-avatar "C"]
       [:span name]
       [:span.mdl-list__item-sub-title (str price-format " - " capacity-format)]]
      [:span.mdl-list__item-secondary-content
       (when-not @editing?
         [:button.mdl-list__item-secondary-action
          {:on-click #(dispatch [:toggle-beer-editing id])}
          "Editar"])]]
     (when @editing?
       [:div.mdl-list__item.beer-item
        [:span.mdl-list__item-primary-content
         [:label "R$"]
         [:input {:type "number"
                  :step "0.01"
                  :on-change #(reset! form-price (-> % .-target .-value))}]
         [:label "ml"]
         [:input {:type "number"
                  :step "1"
                  :on-change #(reset! form-capacity (-> % .-target .-value))}]]
        [:button.mdl-list__item-secondary-action
         {:on-click #(do
                       (dispatch [:change-price id @form-price])
                       (dispatch [:change-capacity id @form-capacity])
                       (dispatch [:toggle-beer-editing id]))}
         "Salvar"]])]))

(defn beer-list [beers]
  [:ul.mdl-list
   (doall (map beer-item beers))])

(defn main-panel []
  (let [beers (subscribe [:all-beers])]
    [:section
     [title]
     [beer-list @beers]
     [results]]))

(defn ^:export init []
  (dispatch-sync [:initialize])
  (r/render [main-panel]
            (js/document.getElementById "app")))
