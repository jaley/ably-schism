(ns ably-schism.frontend.svg
  "Helper functions to generate SVG elements from data

  All `attrs` args can generally contain any SVG attributes you
  want attached to the returned elements. Just provide a map."
  (:require [clojure.string :as str]))

(defn circle
  "Builds an SVG circle element"
  [x y radius & [attrs]]
  [:circle
   (merge {:cx x, :cy y, :r radius} attrs)])

(defn rect
  "Builds an SVG rect element"
  [x y width height & [attrs]]
  [:rect
   (merge {:x x, :y y, :width width, :height height} attrs)])
