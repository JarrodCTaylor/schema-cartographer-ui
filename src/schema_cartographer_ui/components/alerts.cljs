(ns schema-cartographer-ui.components.alerts
  (:require
    [schema-cartographer-ui.utils.helpers :refer [<sub >dis]]
    [schema-cartographer-ui.events :as shared-events]
    [schema-cartographer-ui.subs :as shared-subs]))

(defn alert-display []
  (let [alerts (<sub [::shared-subs/alerts])]
    [:div#alerts
     (for [alert alerts]
       [:div.notification {:key (:uuid alert)
                           :class (str "is-" (:type alert))
                           :on-click #(>dis [::shared-events/remove-alert (:uuid alert)])}
        (:message alert)])]))
