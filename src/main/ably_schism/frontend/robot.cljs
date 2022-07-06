(ns ably-schism.frontend.robot
  "Async loops to execute random mutations against the model"
  (:require [clojure.core.async :as async :refer [<! >!]]))

(defn start!
  "Start the robot mutating model state"
  [mutation-seq interval-ms]
  (let [out-ch (async/chan)]
    (async/go-loop [muts mutation-seq
                    out out-ch
                    time-ch (async/timeout interval-ms)]
      (let [[_ ch] (async/alts! [out time-ch])]
        (when (= ch time-ch)
          (>! out-ch (first muts))
          (recur (rest muts) out (async/timeout interval-ms)))))
    out-ch))
