(ns ably-schism.frontend.ably
  "Ably helpers to connect pub/sub messages to ClojureScript async"
  (:require
   [ably-schism.frontend.shapes :as shapes]
   [ably-schism.secrets.ably :as secrets]
   [cljs.reader :as r]
   [clojure.core.async :as async]
   ["ably" :as ably]))

(def realtime-client
  (memoize
   (fn [node-id]
     (ably/Realtime. #js {:key secrets/ably-api-key
                          :clientId (str node-id)
                          :echoMessages true}))))

(defn attach-publisher!
  "Set up an async publisher worker to push all messages coming through ch
  to Ably for other clients to render them."
  [client ch channel-name]
  (let [ably-chan (.. client -channels (get channel-name))]
    (async/go-loop []
      (when-let [msg (async/<! ch)]
        (.publish ably-chan "state" (pr-str msg))
        (recur)))))

(defn attach-subscriber!
  "Set up an async subscriber to receive incoming messages from other clients
  and push them to ch"
  [client ch channel-name]
  (let [ably-chan (.. client -channels (get channel-name))]
    (.subscribe ably-chan "state"
                (fn [msg]
                  (when-let [msg (r/read-string (.-data msg))]
                    (async/put! ch msg))))))
