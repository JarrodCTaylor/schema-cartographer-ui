(ns schema-cartographer-ui.core
  (:import goog.History)
  (:require
    [day8.re-frame.http-fx]
    [goog.events :as gevents]
    [goog.history.EventType :as EventType]
    [re-frame.core :as rf]
    [reagent.dom :as reagent]
    [reitit.frontend :as rfront]

    [schema-cartographer-ui.components.alerts :refer [alert-display]]
    [schema-cartographer-ui.config :as config]
    [schema-cartographer-ui.utils.form-field-validators]
    [schema-cartographer-ui.events :as shared-events]
    [schema-cartographer-ui.routes.index.subs :as index-subs]
    [schema-cartographer-ui.interceptors :refer [standard-interceptors]]
    [schema-cartographer-ui.router :as router]
    [schema-cartographer-ui.subs :as shared-subs]
    [schema-cartographer-ui.utils.helpers :refer [<sub >dis]]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(rf/reg-fx
  ::hook-browser-navigation
  (fn []
    (doto (History.)
      (gevents/listen
        EventType/NAVIGATE
        (fn [^js event]
          (let [uri (or (not-empty (clojure.string/replace (.-token event) #"^.*#" "")) "/")]
            (>dis [::shared-events/set-active-route (rfront/match-by-path router/routes uri)]))))
      (.setEnabled true))))

(rf/reg-event-fx
  ::initialize-browser-navigation
  [standard-interceptors]
  (fn [_ _]
    {::hook-browser-navigation nil}))

(defn active-route []
  (let [active-route (<sub [::shared-subs/active-route])]
    (>dis [::shared-events/dispatch-route-events active-route])
    (-> active-route :data :view)))

(defn app []
  (let [ready? (<sub [::shared-subs/initialized?])
        {:keys [color-scheme background-color]} (<sub [::index-subs/settings])]
    (if-not ready?
      [:div "Initialising ..."]
      (do
        (>dis [::shared-events/set-color-scheme color-scheme background-color])
        [:<>
         [alert-display]
         [active-route]]))))

(defn mount-app []
  (rf/clear-subscription-cache!)
  (reagent/render [app] (.getElementById js/document "app")))

(defn ^:export main []
  (dev-setup)
  (rf/dispatch-sync [::shared-events/initialize-db])
  (rf/dispatch-sync [::initialize-browser-navigation])
  (mount-app))

(defn ^:dev/after-load ^:after-load on-reload
  "Reload hook to run after the app recompiles
   *NOTE* This has effect of hitting `:dispatch-on-entry` events twice.
   Only a dev time situation so can safely be ignored"
  []
  (let [uri (subs (.-hash js/window.location) 1)]
    (>dis [::shared-events/set-active-route (rfront/match-by-path router/routes uri)])
    (mount-app)))
