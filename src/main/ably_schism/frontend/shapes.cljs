(ns ably-schism.frontend.shapes
  (:require [cljs.reader :as r]
            [ably-schism.frontend.svg :as svg]))


(defprotocol Renderable
  (render [shape] "Return an SVG element for the shape data"))

(defrecord Rect [x y width height styles]
  Renderable
  (render [rect]
    (svg/rect x y width height styles)))

(defrecord Circle [x y radius styles]
  Renderable
  (render [circle]
    (svg/circle x y radius styles)))

(r/register-tag-parser! 'ably-schism.frontend.shapes.Circle map->Circle)
(r/register-tag-parser! 'ably-schism.frontend.shapes.Rect map->Rect)
