(ns ably-schism.frontend.core
  (:require [clojure.core.async :as async]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [schism.core :as s]
            [schism.node :as snode]
            [clojure.core.async :as async :refer [<! >!]]
            [ably-schism.frontend.shapes :as shapes]
            [ably-schism.frontend.mutations :as mut]
            [ably-schism.frontend.model :as model]
            [ably-schism.frontend.robot :as robot]
            [ably-schism.frontend.ably :as ably]))

(def ^:const canvas-width-px 1200)
(def ^:const canvas-height-px 600)
(def ^:const robot-edit-interval-ms 1000)
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
      (robot/start! model mutations robot-edit-interval-ms)
      (async/close! ch))))

(defn toolbar
  "Simple controls to stop and start robot and sync"
  [model robot-ch]
  [:div.toolbar
   [:button.toolbar-button
    {:on-click (fn [_] (swap! robot-ch toggle-robot! model))}
    (if @robot-ch "Stop Robot" "Start Robot")]
   [:button.toolbar-button "Start Sync"]])

(defn page-root
  "Landing page boiler plate"
  [model]
  (let [robot-ch (r/atom nil)]
    [:div.root
     [:div.head
      [:h1 "Ably/Schism Demo"]
      [toolbar model robot-ch]]
     [:div.main
      [canvas model]]]))


(defn mount-root
  "Attach reagent to the root element"
  [root-id model]
  (rdom/render
   [page-root model]
   (.getElementById js/document root-id)))

(defn exchange
  "Send state updates to Ably channel and subscribe to remote updates"
  [model mut-ch]
  (let [ably-client (ably/realtime-client snode/*current-node*)
        in-ch (async/chan)
        out-ch (async/chan)]
    (ably/attach-publisher! ably-client out-ch ably-channel-name)
    (async/go-loop [mutation (<! mut-ch)]
      (>! out-ch (mut/mutate mutation @model))
      (recur (<! mut-ch)))

    (ably/attach-subscriber! ably-client in-ch ably-channel-name)
    (async/go-loop [incoming (<! in-ch)]
      (swap! model s/converge incoming)
      (recur (<! in-ch)))))

(defn ^:export init
  "Entry point to attach page to root"
  []
  (snode/initialize-node!)
  (let [model (model/init "test")]
    (mount-root "root" model)))
