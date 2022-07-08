(ns ably-schism.frontend.core
  (:require [clojure.core.async :as async]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [schism.core :as s]
            [schism.node :as snode]
            [ably-schism.frontend.shapes :as shapes]
            [ably-schism.frontend.mutations :as mut]
            [ably-schism.frontend.model :as model]
            [ably-schism.frontend.robot :as robot]
            [ably-schism.frontend.ably :as ably]))

(def ^:const canvas-width-px 800)
(def ^:const canvas-height-px 400)
(def ^:const ably-channel-name "canvas-state")

(defn canvas
  "Return an SVG canvas element, rendering content from model"
  [model]
  [:div.canvas
   [:svg
    {:width (str canvas-width-px "px")
     :height (str canvas-height-px "px")}
    (for [[id shape] @model]
      ^{:key id} [shapes/render shape])]])

(defn toggle-robot!
  "Start the robot if ch is nil and return a new stop-chan.
  If ch is a channel, close it to stop the robot and return nil."
  [ch model]
  (let [mutations (mut/mutation-seq canvas-width-px canvas-height-px)]
    (if (nil? ch)
      (robot/start! model mutations)
      (async/close! ch))))

(defn toggle-sync!
  "Start sync if ch is nil and return a channel to stop it.
  Stop sync if ch is non-nil and return nil."
  [ch model]
  (if (nil? ch)
    (ably/sync! model ably-channel-name)
    (async/close! ch)))

(defn toolbar
  "Simple controls to stop and start robot and sync"
  [model robot-ch sync-ch]
  [:div.toolbar
   [:button.toolbar-button
    {:on-click #(swap! robot-ch toggle-robot! model)}
    (if (nil? @robot-ch) "Start Robot" "Stop Robot")]
   [:button.toolbar-button
    {:on-click #(swap! sync-ch toggle-sync! model)}
    (if (nil? @sync-ch) "Start Sync" "Stop sync")]])

(defn page-root
  "Landing page boiler plate"
  [model]
  (let [robot-ch (r/atom nil)
        sync-ch (r/atom nil)]
    [:div.root
     [:div.head
      [:h1 "Ably/Schism Demo"]
      [toolbar model robot-ch sync-ch]]
     [:div.main
      [canvas model]]]))

(defn mount-root
  "Attach reagent to the root element"
  [root-id model]
  (rdom/render
   [page-root model]
   (.getElementById js/document root-id)))

(defn ^:export init
  "Entry point to attach page to root"
  []
  (snode/initialize-node!)
  (let [model (model/init "test")]
    (mount-root "root" model)))
