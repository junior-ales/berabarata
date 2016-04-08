(ns berabarata.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core  :as    r]
            [re-frame.core :refer [dispatch
                                   dispatch-sync
                                   subscribe]]))

(defn title []
  [:header
   [:h4 "Lista de Compras"]])

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
