(ns ably-schism.frontend.ably
  "Ably helpers to connect pub/sub messages to ClojureScript async"
  (:require
   [ably-schism.frontend.shapes :as shapes]
   [ably-schism.secrets.ably :as secrets]
   [cljs.reader :as r]
   [clojure.core.async :as async :refer [<! >!]]
   [schism.core :as s]
   [schism.node :as snode]
   ["ably" :as ably]))

(def ^:const sync-interval-ms 2500)

(def ^:private realtime-client
  (memoize
   (fn [node-id]
     (ably/Realtime. #js {:key secrets/ably-api-key
                          :clientId (str node-id)
                          :echoMessages false}))))

(defn- attach-publisher!
  "Set up an async publisher worker to push all messages coming through the
  returned channel to Ably on given channel-name."
  [client channel-name]
  (let [ch (async/chan)
        ably-chan (.. client -channels (get channel-name))]
    (async/go-loop [last-msg nil
                    msg (<! ch)]
      (when (not= msg last-msg)
        (.publish ably-chan "state" (pr-str msg)))
      (recur msg (<! ch)))
    ch))

(defn- attach-subscriber!
  "Set up an async subscriber to receive incoming messages from other clients
  and push them onto the returned channel"
  [client channel-name]
  (let [ch (async/chan)
        chan-param (str "[?rewind=1]" channel-name)
        ably-chan (.. client -channels (get chan-param))]
    (.subscribe ably-chan "state"
                (fn [msg]
                  (when-let [msg (r/read-string (.-data msg))]
                    (async/put! ch msg))))
    ch))


(defn sync!
  "Connect to Ably ad synchronise this model with remote clients
  periodically using swap & converge. Returns a channel, which can
  be closed to kill the sync process."
  [model channel-name]
  (let [conn (realtime-client snode/*current-node*)
        stop-ch (async/chan)
        in-ch (attach-subscriber! conn channel-name)
        out-ch (attach-publisher! conn channel-name)]
    (async/go-loop [time-ch (async/timeout sync-interval-ms)]
      (let [[msg ch] (alts! [stop-ch time-ch in-ch])]
        (when (not= ch stop-ch)
          (condp = ch
            time-ch (>! out-ch @model)
            in-ch (swap! model s/converge msg))
          (recur (async/timeout sync-interval-ms)))))
    stop-ch))
