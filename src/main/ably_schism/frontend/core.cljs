(ns ably-schism.frontend.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [ably-schism.frontend.shapes :as shapes]
            [ably-schism.frontend.model :as model]))

(defn canvas
  "Return an SVG canvas element, rendering content from model"
  [model]
  [:div.canvas
   [:svg
    {:width "1200px"
     :height "600px"}
    (for [[id shape] @model]
      ^{:key id} [shapes/render shape])]])

(defn page-root
  "Landing page boiler plate"
  [model]
  [:div.root
   [:div.head
    [:h1 "Ably/Schism Demo"]]
   [:div.main
    [canvas model]]])


(defn mount-root
  "Attach reagent to the root element"
  [root-id model]
  (rdom/render
   [page-root model]
   (.getElementById js/document root-id)))

(defn ^:export init
  "Entry point to attach page to root"
  []
  (let [model (model/init "test")]
    (mount-root "root" model)))
