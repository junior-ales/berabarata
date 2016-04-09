(ns berabarata.components
  (:require [reagent.core  :as    r]
            [re-frame.core :refer [dispatch subscribe]]
            [clojure.string :refer [join blank?]]))

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

(defn beer-item-display [{:keys [id name price capacity editing? enabled?]}]
  (let [price-format (when-not (zero? price)
                       (str "R$ " (.toFixed price 2)))
        capacity-format (when-not (zero? capacity)
                          (str capacity "ml"))
        item-info (when (or price-format capacity-format)
                    (join " ⸳ " (remove blank? [price-format capacity-format])))
        default-classes "item-wrapper mdl-list__item mdl-list__item--two-line"]
    [:div {:class (str default-classes (when-not enabled? (str " -disabled")))}
     [:span.mdl-list__item-primary-content
      [:i.checkbox-avatar.mdl-list__item-avatar
       [:label.label.mdl-checkbox.mdl-js-checkbox.mdl-js-ripple-effect
        {:for (str id "-item-checkbox")}
        [:input.mdl-checkbox__input
         {:id (str id "-item-checkbox")
          :type "checkbox"
          :on-change #(dispatch [:toggle-item-state id])}]]]
      [:span.item-name name]
      [:span.mdl-list__item-sub-title item-info]]
     [:span.mdl-list__item-secondary-content
      (when-not editing?
        [:button.edit-button.mdl-list__item-secondary-action
         {:on-click #(dispatch [:toggle-item-editing id])}
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
      [:div.item-wrapper.mdl-list__item
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
                      (dispatch [:toggle-item-editing id]))}
        [:img.icon {:src "./images/icon-done.png" }]]])))

(defn beer-item [beer]
  [:li {:key (str (:id beer) "-item")}
   [beer-item-display beer]
   [beer-item-edit beer]])

(defn new-item-field []
  [:div.item-wrapper.-new.mdl-list__item {:on-click #(dispatch [:edit-new-item])}
   [:span.mdl-list__item-primary-content
    [:i.new-item-avatar.material-icons.mdl-list__item-avatar
     [:span.icon "✚"]]
    [:span.label "Novo item..."]]])

(defn new-item-field-editing []
  (let [form-new-name (r/atom "")]
    [:div.item-wrapper.-new.mdl-list__item
     [:span.mdl-list__item-primary-content
      [:i.new-item-avatar.material-icons.mdl-list__item-avatar
       [:span.icon
        {:on-click #(dispatch [:edit-new-item])} "✖"]]
      [input-field {:id "edit-new-item-name"
                    :autofocus? true
                    :type "text"
                    :input-atom form-new-name
                    :label "Nome"}]]
     [:button.save-button.mdl-list__item-secondary-action
      {:on-click #(dispatch [:create-new-item @form-new-name])}
      [:img.icon {:src "./images/icon-done.png" }]]]))

(defn new-item []
  (let [editing-new-item? (subscribe [:editing-new-item?])]
    (if @editing-new-item?
      [new-item-field-editing]
      [new-item-field])))

(defn beer-list [beers]
  [:ul.mdl-list
   (map beer-item beers)
   [new-item]])
