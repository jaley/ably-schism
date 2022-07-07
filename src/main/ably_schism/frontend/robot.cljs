(ns ably-schism.frontend.robot
  "Async loops to execute random mutations against the model"
  (:require [clojure.core.async :as async :refer [<! >!]]
            [ably-schism.frontend.mutations :as mut]))

(def ^:const edit-interval-ms 500)

(defn start!
  "Start the robot mutating model using the given sequence of mutations.
  Returns a channel, which can be closed to stop the robot."
  [model mutation-seq]
  (let [stop-ch (async/chan)]
    (async/go-loop [muts mutation-seq
                    time-ch (async/timeout edit-interval-ms)]
      (let [[_ ch] (async/alts! [stop-ch time-ch])]
        (when (= ch time-ch)
          (swap! model (partial mut/mutate (first muts)))
          (recur (rest muts) (async/timeout edit-interval-ms)))))
    stop-ch))
