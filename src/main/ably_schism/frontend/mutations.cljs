(ns ably-schism.frontend.mutations
  "Random edits that can be made to the model state by bots"
  (:require [ably-schism.frontend.shapes :as shapes]))

(defprotocol Mutation
  (mutate [mutation state]
    "Execute this mutation against the model state"))

(defrecord UpdateStyle [style]
  Mutation
  (mutate [this state]
    (if-let [shape-id (-> state keys rand-nth)]
      (update-in state [shape-id :style] (constantly style))
      state)))

(defrecord AddShape [shape]
  Mutation
  (mutate [this state]
    (assoc state (random-uuid) shape)))

(defrecord DeleteShape []
  Mutation
  (mutate [this state]
    (if-let [shape-id (-> state keys rand-nth)]
      (dissoc state shape-id)
      state)))

(defn- random-style
  "Returns a style map with a random fill color"
  []
  {:fill (rand-nth ["blue" "green" "red" "orange"
                    "yellow" "purple" "pink"])})

(defn- random-update
  "Returns a random update mutation"
  []
  (->UpdateStyle (random-style)))

(defn- random-rect
  "Returns a randomly sized and styled rectangle"
  [canvas-width canvas-height]
  (let [min-max (fn [dim] [(/ dim 10) (/ dim 4)])
        [w-min w-max] (min-max canvas-width)
        [h-min h-max] (min-max canvas-height)]
    (shapes/->Rect
     (rand-int canvas-width)
     (rand-int canvas-height)
     (+ w-min (rand-int (- w-max w-min)))
     (+ h-min (rand-int (- h-max h-min)))
     (random-style))))

(defn- random-circle
  "Returns a randomly sized and styled circle"
  [canvas-width canvas-height]
  (shapes/->Circle
   (rand-int canvas-width)
   (rand-int canvas-height)
   (+ 10 (rand-int 40))
   (random-style)))

(defn- random-add-shape
  "Return a mutation to add a random shape to the model state"
  [canvas-width canvas-height]
  (let [shape (rand-nth [random-rect random-circle])]
    (->AddShape (shape canvas-width canvas-height))))

(defn random-mutation
  "Returns a Mutation to make a random change to model state"
  [canvas-width canvas-height]
  (let [mutations [(partial random-add-shape canvas-width canvas-height)
                   random-update
                   ->DeleteShape]]
    (-> mutations rand-nth .call)))
