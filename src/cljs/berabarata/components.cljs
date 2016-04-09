(ns berabarata.components
  (:require [reagent.core  :as    r]
            [re-frame.core :refer [dispatch subscribe]]))

(defn title []
  [:header
   [:h4.title "Lista de Compras"]])

(defn interpose-sep [separator items]
  (apply str (interpose separator (remove nil? items))))

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
          [:td.mdl-data-table__cell--non-numeric (interpose-sep " ⸳ "
                                                                [(:name @best-beer) (:brand @best-beer)])]
          [:td (str (:capacity @best-beer) "ml")]
          [:td (str "R$" (-> liter-price (.toFixed 2)))]]]]])))

(defn input-field [{:keys [id type autofocus? step input-atom label]}]
  [:div.edit-item-box.mdl-textfield.mdl-js-textfield.mdl-textfield--floating-label.is-focused
   [:input {:id id
            :class "mdl-textfield__input"
            :type type
            :auto-focus (or autofocus? false)
            :step step
            :on-change #(reset! input-atom (-> % .-target .-value))}]
   [:label {:class "mdl-textfield__label" :for id} label]])

(defn display-item [{:keys [id name brand price capacity comparing? editing? enabled?]}]
  (let [form-name       (r/atom "")
        price-format    (when-not (zero? price)
                          (str "R$ " (.toFixed price 2)))
        capacity-format (when-not (zero? capacity)
                          (str capacity "ml"))
        item-info       (when (or brand price-format capacity-format)
                          (interpose-sep " ⸳ " [brand price-format capacity-format]))]
    [:div {:class (str "item-wrapper mdl-list__item"
                       (when editing? " -editing")
                       (when-not enabled? " -disabled")
                       (when-not (nil? item-info) " -with-info mdl-list__item--two-line"))}
     (if editing?
       [:span.mdl-list__item-primary-content
        [:i.item-avatar.material-icons.mdl-list__item-avatar
         [:span.icon
          {:on-click #(dispatch [:toggle-item-editing id])} "✖"]]
        [input-field {:id (str id "edit-item-name")
                      :type "text"
                      :autofocus? true
                      :input-atom form-name
                      :label "Nome"}]]
       [:span.mdl-list__item-primary-content
        [:input {:class "toggle-item-status"
                 :type "checkbox"
                 :checked (not enabled?)
                 :on-change #(dispatch [:toggle-item-state id])}]
        [:span {:on-click #(dispatch [:toggle-item-editing id])}
         [:span.item-name name]
         [:span.item-info.mdl-list__item-sub-title item-info]]])
     (when editing?
       [:button.save-button.mdl-list__item-secondary-action
        {:on-click #(do
                      (dispatch [:change-name id @form-name])
                      (dispatch [:toggle-item-editing id]))}
        [:img.icon {:src "./images/icon-done.png" }]])
     (when-not (or editing? comparing?)
       [:button.edit-button.mdl-list__item-secondary-action
        {:on-click #(dispatch [:toggle-item-comparing id])}
        [:img.icon {:src "./images/icon-more.png" }]])]))

(defn compare-item [{:keys [id price capacity comparing?]}]
  (let [form-brand (r/atom "")
        form-price (r/atom 0)
        form-capacity (r/atom 0)]
    (when comparing?
      [:div.item-wrapper.-comparing.mdl-list__item
       [:span.mdl-list__item-primary-content
        [input-field {:id (str id "edit-item-brand")
                      :type "text"
                      :autofocus? true
                      :input-atom form-brand
                      :label "Marca"}]
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
                      (dispatch [:change-brand id @form-brand])
                      (dispatch [:change-price id @form-price])
                      (dispatch [:change-capacity id @form-capacity])
                      (dispatch [:toggle-item-comparing id]))}
        [:img.icon {:src "./images/icon-done.png" }]]])))

(defn make-item [item]
  [:li {:key (str (:id item) "-item")}
   [display-item item]
   [compare-item item]])

(defn new-item []
  [:div.item-wrapper.-new.mdl-list__item {:on-click #(dispatch [:edit-new-item])}
   [:span.mdl-list__item-primary-content
    [:i.item-avatar.material-icons.mdl-list__item-avatar
     [:span.icon "✚"]]
    [:span.label "Novo item..."]]])

(defn edit-new-item []
  (let [form-new-name (r/atom "")]
    [:div.item-wrapper.-new.mdl-list__item
     [:span.mdl-list__item-primary-content
      [:i.item-avatar.material-icons.mdl-list__item-avatar
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

(defn add-new-item []
  (let [editing-new-item? (subscribe [:editing-new-item?])]
    (if @editing-new-item?
      [edit-new-item]
      [new-item])))

(defn item-list [items]
  [:ul.mdl-list
   (map make-item items)
   [add-new-item]])
