(ns typing-ex.typing
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs-http.client :as http]
   [cljs.reader :refer [read-string]]
   [cljs.core.async :refer [<!]]
   [clojure.string :as str]
   [reagent.core :refer [atom]]
   [reagent.dom :as rdom]
   [typing-ex.plot :refer [plot]]))

(def ^:private version "1.5.8")
(def ^:private timeout 60)

(defonce app-state (atom {:text "wait a little"
                          :answer ""
                          :seconds timeout
                          :errors 0}))

(defonce first-key (atom false))

(defonce todays (atom {}))

(defn get-login []
  (-> (.getElementById js/document "login")
      (.-value)))

(defn reset-app-state! []
  (go (let [drill (<! (http/get (str "/drill")))
            {scores :body} (<! (http/get (str "/todays/" (get-login))))]
        (swap! app-state assoc :text (:body drill)
               :answer ""
               :seconds timeout
               :errors 0)
        ;;(.log js/console "text" s)
        (reset! first-key false)
        (reset! todays (->> (read-string scores)
                            (map #(assoc % :pt (max 0 (:pt %))))))
        (.focus (.getElementById js/document "drill")))))

;;; pt will not be nagative.
(defn pt-raw [{:keys [text answer seconds errors]}]
  (let [s1 (str/split text #"\s+")
        s2 (str/split answer #"\s+")
        s1<>s2 (map list s1 s2)
        all (count s1)
        goods (count (filter (fn [[x y]] (= x y)) s1<>s2))
        bads  (count (remove (fn [[x y]] (= x y)) s1<>s2))
        err   (* -1 errors errors)
        score (int (* 100 (- (/ goods all) (/ bads goods))))]
    (js/console.log "goods bads all error score: " goods bads all err score)
    (if (= all (+ goods bads))
      (+ score err seconds)
      (+ score err))))

(defn pt [args]
  (max 0 (pt-raw args)))

(defonce todays-count (atom 0))
(def ^:private todays-max 10)

(defn login-pt-message [{:keys [pt login]}]
  (let [s1 (str login " さんのスコアは " pt " 点です。")
        s2 (condp <= pt
             100 "すばらしい。最高点取れた？平均で 80 点越えよう。"
             90 "がんばった。もう少しで 100 点だね。"
             60 "だいぶ上手です。この調子でがんばれ。"
             30 "指先を見ずに、ゆっくり、ミスを少なく。"
             "練習あるのみ。")]
    (swap! todays-count inc)
    (if (< todays-max @todays-count)
      (do
        (reset! todays-count 0)
        (str s1 "\n" s2 "\n" "いったん休憩入れよう 🍵"))
      (str s1 "\n" s2))))

(defn send-score! []
  (go (let [token (-> (js/document.getElementById "__anti-forgery-token")
                      .-value)
            resp (<! (http/post
                      "/score"
                      {:form-params
                       {:pt (pt @app-state)
                        :__anti-forgery-token token}}))]
        (js/alert (login-pt-message (read-string (:body resp)))))))

(defn count-down []
  (when true ;; @first-key
    (swap! app-state update :seconds dec))
  (when (zero? (:seconds @app-state))
    ;; 1.5.7
    (if (zero? (count (:answer @app-state)))
      (js/alert "タイプ忘れた？")
      (send-score!))
    (reset-app-state!)))

;; FIXME: when moving below block to top of this code,
;;        becomes not counting down.
;;(declare count-down)
(defonce updater (js/setInterval count-down 1000))

;; FIXME: function name
(defn show-sorry [n]
  (take n (repeat "🙅"))) ;;🙅💧💦💔❌🦠🥶🥺

(defn check-key [key]
  ;; (when-not @first-key
  ;;   (swap! first-key not))
  (when (= key "Backspace")
    (swap! app-state update :errors inc)))

(defn error-component []
  [:p "　" (show-sorry (:errors @app-state))])

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
             :style {:font-family "monospace"}
             :value (:seconds @app-state)
             :on-click #(do (send-score!) (reset-app-state!))}]
    " 🔚全部打ち終わってクリックするとボーナス"]
   [:p
    "Your todays:"
    [:br]
    [plot 300 150 @todays]]
   ;;
   [:p
    [:a {:href "/sum/1" :class "btn btn-primary btn-sm"} "D.P."]
    " "
    [:a {:href "/logout" :class "btn btn-warning btn-sm"} "logout"]]
   [:hr]
   [:div "hkimura, " version]])

(defn start []
  (rdom/render [ex-page] (js/document.getElementById "app"))
  (.focus (.getElementById js/document "drill")))

(defn ^:export init []
  (reset-app-state!)
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))
