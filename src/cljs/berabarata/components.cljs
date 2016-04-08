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
        [:button.mdl-list__item-secondary-action
         {:on-click #(dispatch [:toggle-beer-editing id])}
         "Editar"])]]))

(defn beer-item-edit [{:keys [id price capacity editing?]}]
  (let [form-name (r/atom "")
        form-price (r/atom 0)
        form-capacity (r/atom 0)]
    (when editing?
      [:div.edit-item-wrapper.mdl-list__item
       [:span.mdl-list__item-primary-content
        [:div.edit-item-box.mdl-textfield.mdl-js-textfield.mdl-textfield--floating-label.is-focused
         [:input {:id (str id "edit-item-name")
                  :class "mdl-textfield__input"
                  :type "text"
                  :auto-focus true
                  :on-change #(reset! form-name (-> % .-target .-value))}]
         [:label
          {:class "mdl-textfield__label" :for (str id "edit-item-name")}
          "Item/Marca"]]
        [:div.edit-item-box.mdl-textfield.mdl-js-textfield.mdl-textfield--floating-label.is-focused
         [:input {:id (str id "edit-item-price")
                  :class "mdl-textfield__input"
                  :type "number"
                  :step "0.01"
                  :on-change #(reset! form-price (-> % .-target .-value))}]
         [:label
          {:class "mdl-textfield__label" :for (str id "edit-item-price")}
          "Preço (R$)"]]
        [:div.edit-item-box.mdl-textfield.mdl-js-textfield.mdl-textfield--floating-label.is-focused
         [:input {:id (str id "edit-item-capacity")
                  :class "mdl-textfield__input"
                  :type "number"
                  :step "1"
                  :on-change #(reset! form-capacity (-> % .-target .-value))}]
         [:label
          {:class "mdl-textfield__label" :for (str id "edit-item-capacity")}
          "Tamanho"]]]
       [:button.mdl-list__item-secondary-action
        {:on-click #(do
                      (dispatch [:change-name id @form-name])
                      (dispatch [:change-price id @form-price])
                      (dispatch [:change-capacity id @form-capacity])
                      (dispatch [:toggle-beer-editing id]))}
        "Salvar"]])))

(defn beer-item [{:keys [id] :as beer}]
  [:li {:key (str id "-item")}
   [beer-item-display beer]
   [beer-item-edit beer]])

(defn beer-list [beers]
  [:ul.mdl-list
   (map beer-item beers)])
