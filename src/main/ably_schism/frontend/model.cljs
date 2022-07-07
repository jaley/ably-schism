(ns ably-schism.frontend.model
  (:require [ably-schism.frontend.shapes :as shapes]
            [reagent.core :as r]
            [schism.core :as s]
            [schism.node :as snode]))

(defn init
  "Returns a reagent atom with model state for given channel name,
  which will be swapped as remote changes converge"
  [channel-name]
  (r/atom
   (s/convergent-map
    snode/*current-node* (shapes/->Blank snode/*current-node*))))
