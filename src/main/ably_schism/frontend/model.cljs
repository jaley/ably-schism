(ns ably-schism.frontend.model
  (:require [ably-schism.frontend.shapes :as shapes]
            [reagent.core :as r]
            [schism.core :as s]))

(defn init
  "Returns a reagent atom with model state for given channel name,
  which will be swapped as remote changes converge"
  [channel-name]
  (r/atom
   (s/convergent-map
    :shapes [(shapes/->Circle 100 100 50 {:fill "red"})
             (shapes/->Rect 300 300 150 250 {:fill "green"})])))
