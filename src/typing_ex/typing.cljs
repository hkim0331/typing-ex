(ns typing-ex.typing
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs-http.client :as http]
   [cljs.core.async :refer [<!]]
   [clojure.edn :refer [read-string]]
   [clojure.string :as str]
   [reagent.core :refer [atom]]
   [reagent.dom :as rdom]))

(def ^:private version "1.2.4")

(defonce app-state (atom {:text "wait a little"
                          :answer ""
                          :seconds 60
                          :errors 0}))


(defonce first-key (atom false))

;; report-alert 回数練習したら一度、アラートを出す。
;; この場所で定義するのがいいのか？
;; (defonce how-many-typing (atom 0))
;; (def ^:private report-alert 10)

(defn reset-app-state! []
  (go (let [response (<! (http/get (str "/drill/" 0)))]
        (swap! app-state assoc :text (:body response))))
  (swap! app-state assoc :answer "" :seconds 60 :errors 0)
  (reset! first-key false))

(defn pt [{:keys [text answer seconds errors]}]
  (let [s1 (str/split text #"\s+")
        s2 (str/split answer #"\s+")
        s1<>s2 (map list s1 s2)
        all (count s1)
        goods (count (filter (fn [[x y]] (= x y)) s1<>s2))
        bads  (count (remove (fn [[x y]] (= x y)) s1<>s2))
        err   (* -1 (* errors errors))
        score (int (* (- (/ goods all) (/ bads goods)) 100))]
    (js/console.log "goods bads all error score: " goods bads all err score)
    (if (= all (+ goods bads))
      (+ score err seconds)
      (+ score err))))

(defn login-pt-message [{:keys [pt login]}]
  (let [s1 (str login " さんのスコアは " pt " 点です。")
        s2 (condp <= pt
             100 "すんばらしい。最高点取れた？平均で 80 点越えよう。"
             90 "がんばった。もう少しで 100 点だね。"
             60 "だいぶ上手です。この調子でがんばれ。"
             30 "指先を見ずに、ゆっくり、ミスを少なく。"
             "練習あるのみ。")]
    (str s1 "\n" s2)))

;; it worked!
(defn send-score []
  (go (let [token (.-value (js/document.getElementById "__anti-forgery-token"))
            response (<! (http/post
                          "/score"
                          {:form-params
                            {:pt (pt @app-state)
                             :__anti-forgery-token token}}))]
        (js/alert (login-pt-message (read-string (:body response))))
        (reset-app-state!))))
  ;; (swap! how-many-typing inc)
  ;; (when (= 0 (mod @how-many-typing report-alert))
  ;;   (js/alert "がんばってんねー。一旦、休憩入れたら？")))

(defn count-down []
  (when @first-key
    (swap! app-state update :seconds dec))
  (when (zero? (:seconds @app-state))
    ;;(swap! app-state update :seconds constantly 0)
    (send-score)))

(defn by-dots [n]
  (take n (repeat "🥶"))) ;;🙅💧💦💔❌🦠🥶🥺

(defn check-key [key]
  (when-not @first-key
    (swap! first-key not))
  (when (= key "Backspace")
    (swap! app-state update :errors inc)))

(defn error-component []
  [:p "　" (by-dots (:errors @app-state))])

(defn ex-page []
  [:div
   [:h2 "Typing: Challenge"]
   [:p
    {:class "red"}
    "指先見ないで、ゆっくり、確実に。単語間のスペースは一個でよい。"]
   [:pre {:id "example"} (:text @app-state)]
   [:textarea {:name "answer"
               :id "drill"
               :value (:answer @app-state)
               :on-key-up #(check-key (.-key %))
               :on-change #(swap! app-state
                                  assoc
                                  :answer
                                  (-> % .-target .-value))}]
   [error-component]
   [:p
    [:input {:type  "button"
             :id    "seconds"
             :class "btn btn-success btn-sm"
             :value (:seconds @app-state)
             :on-click send-score}] " 🔚全部打ち終わってクリックするとボーナス"]
   [:p
    [:a {:href "/scores" :class "btn btn-primary btn-sm"} "scores"]
    " "
    [:a {:href "/logout" :class "btn btn-warning btn-sm"} "logout"]]
   [:hr]
   [:div "hkimura, " version]])

(defn start []
  (rdom/render [ex-page] (js/document.getElementById "app"))
  (js/setInterval count-down 1000))

(defn ^:export init []
  (reset-app-state!)
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop")
  (js/setInterval count-down 9999999))
