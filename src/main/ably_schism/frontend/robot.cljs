(ns ably-schism.frontend.robot
  "Async loops to execute random mutations against the model"
  (:require [clojure.core.async :as async :refer [<! >!]]
            [ably-schism.frontend.mutations :as mut]))

;; TODO: find a better place to put all these canvas dims

(defn start!
  "start the robot mutating model state"
  [model canvas-width canvas-height interval-ms]
  (let [out-ch (async/chan)]
    (async/go-loop [out out-ch
                    time-ch (async/timeout interval-ms)]
      (let [[_ ch] (async/alts! [out time-ch])]
        (when (= ch time-ch)
          (let [mutation (mut/random-mutation canvas-width canvas-height)]
            (swap! model (partial mut/mutate mutation))
            (recur out (async/timeout interval-ms))))))))
