(ns berabarata.components
  (:require [reagent.core  :as    r]
            [re-frame.core :refer [dispatch subscribe]]))

(defn title []
  [:header
   [:h4.title "Lista de Compras"]])

(defn results []
  (let [best-beer (subscribe [:best-beer])
        liter-price (or (:liter-price @best-beer) 0)]
    (when-not (zero? liter-price)
      [:section
       [:h5 "Resultado"]
       [:table.mdl-data-table.mdl-shadow--2dp.results-table
        [:thead
         [:tr
          [:th.mdl-data-table__cell--non-numeric "Nome"]
          [:th "Embalagem"]
          [:th "Preço Litro"]]]
        [:tbody
         [:tr
          [:td.mdl-data-table__cell--non-numeric (:name @best-beer)]
          [:td (str (:capacity @best-beer) "ml")]
          [:td (str "R$" (-> liter-price (.toFixed 2)))]]]]])))

(defn beer-item-display [{:keys [id name price capacity editing?]}]
  (let [price-format (str "R$ " (.toFixed price 2))
        capacity-format (str capacity "ml")]
    [:div.show-item-wrapper.mdl-list__item.mdl-list__item--two-line
     [:span.mdl-list__item-primary-content
      [:i.material-icons.mdl-list__item-avatar "C"]
      [:span name]
      [:span.mdl-list__item-sub-title (str price-format " - " capacity-format)]]
     [:span.mdl-list__item-secondary-content
      (when-not editing?
        [:button.edit-button.mdl-list__item-secondary-action
         {:on-click #(dispatch [:toggle-beer-editing id])}
         [:img.icon {:src "./images/icon-more.png" }]])]]))

(defn input-field [{:keys [id type autofocus? step input-atom label]}]
  [:div.edit-item-box.mdl-textfield.mdl-js-textfield.mdl-textfield--floating-label.is-focused
   [:input {:id id
            :class "mdl-textfield__input"
            :type type
            :auto-focus (or autofocus? false)
            :step step
            :on-change #(reset! input-atom (-> % .-target .-value))}]
   [:label {:class "mdl-textfield__label" :for id} label]])

(defn beer-item-edit [{:keys [id price capacity editing?]}]
  (let [form-name (r/atom "")
        form-price (r/atom 0)
        form-capacity (r/atom 0)]
    (when editing?
      [:div.edit-item-wrapper.mdl-list__item
       [:span.mdl-list__item-primary-content
        [input-field {:id (str id "edit-item-name")
                      :type "text"
                      :autofocus? true
                      :input-atom form-name
                      :label "Item/Marca"}]
        [input-field {:id (str id "edit-item-price")
                      :type "number"
                      :step "0.01"
                      :input-atom form-price
                      :label "Preço (R$)"}]
        [input-field {:id (str id "edit-item-capacity")
                      :type "number"
                      :step "1"
                      :input-atom form-capacity
                      :label "Tamanho"}]]
       [:button.save-button.mdl-list__item-secondary-action
        {:on-click #(do
                      (dispatch [:change-name id @form-name])
                      (dispatch [:change-price id @form-price])
                      (dispatch [:change-capacity id @form-capacity])
                      (dispatch [:toggle-beer-editing id]))}
        [:img.icon {:src "./images/icon-done.png" }]]])))

(defn beer-item [{:keys [id] :as beer}]
  [:li {:key (str id "-item")}
   [beer-item-display beer]
   [beer-item-edit beer]])

(defn beer-list [beers]
  [:ul.mdl-list
   (map beer-item beers)])
