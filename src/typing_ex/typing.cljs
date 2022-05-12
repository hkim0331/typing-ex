(ns typing-ex.typing
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs-http.client :as http]
   [cljs.reader :refer [read-string]]
   [cljs.core.async :refer [<!]]
   [clojure.string :as str]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [taoensso.timbre :as timbre]
   [typing-ex.plot :refer [plot]]))

(def ^:private version "1.7.0-SNAPSHOT")

(def ^:private timeout 20)
(def ^:private todays-max 10)

(defonce todays-trials (r/atom 0))
(defonce app-state (r/atom {}))
(defonce todays (r/atom {}))

(defn get-login []
  (-> (.getElementById js/document "login")
      (.-value)))

(defn reset-app-state! []
  (go (let [{drill :body}  (<! (http/get (str "/drill")))
            words (str/split drill #"\s+")
            {scores :body} (<! (http/get (str "/todays/" (get-login))))]
        (reset! todays (read-string scores))
        (reset! app-state
                {:text drill
                 :answer ""
                 :seconds timeout
                 :errors 0
                 :words words
                 :words-max (count words)
                 :pos 0
                 :results []})
        (.focus (.getElementById js/document "drill")))))

;;; pt must not be nagative.
(defn pt-raw [{:keys [text answer seconds errors]}]
  (let [s1 (str/split text #"\s+")
        s2 (str/split answer #"\s+")
        s1<>s2 (map list s1 s2)
        all (count s1)
        goods (count (filter (fn [[x y]] (= x y)) s1<>s2))
        bads  (count (remove (fn [[x y]] (= x y)) s1<>s2))
        err   (* -1 errors errors)
        score (int (* 100 (- (/ goods all) (/ bads goods))))]
    ;;(js/console.log "goods bads all error score: " goods bads all err score)
    (timbre/info (get-login) goods bads all err score)
    (if (= all (+ goods bads))
      (+ score err seconds)
      (+ score err))))

(defn pt [args]
  (max 0 (pt-raw args)))

(defn intermission-message [{:keys [pt login]}]
  (let [s1 (str login " さんのスコアは " pt " 点です。")
        s2 (condp <= pt
             100 "すばらしい。最高点取れた？平均で 80 点越えよう。"
             90 "がんばった。もう少しで 100 点だね。"
             60 "だいぶ上手です。この調子でがんばれ。"
             30 "指先を見ずに、ゆっくり、ミスを少なく。"
             "練習あるのみ。")]
    (js/alert s1 "\n" s2)))

(defn send-score! []
  (go (let [token (-> (js/document.getElementById "__anti-forgery-token")
                      .-value)
            {body :body} (<! (http/post
                              "/score"
                              {:form-params
                               {:pt (pt @app-state)
                                :__anti-forgery-token token}}))]
        (intermission-message (read-string body))
        (swap! todays-trials inc)
        (when (zero? (mod @todays-trials todays-max))
          (js/alert "いったん休憩入れよう 🍵")))))

(defn countdown []
  (swap! app-state update :seconds dec)
  (when (zero? (:seconds @app-state))
    (if (zero? (count (:answer @app-state)))
      (js/alert "タイプ忘れた？")
      (send-score!))
    (reset-app-state!)))

;; FIXME: when moving below block to top of this code,
;;        becomes not counting down even if declared.
;;(declare countdown)
(defonce updater (js/setInterval countdown 1000))

(defn check-word []
  (let [target (get (@app-state :words) (@app-state :pos))
        typed  (last (str/split (@app-state :answer) #"\s+"))]
    (.log js/console target typed)
    (swap! app-state update :results
           #(conj % (if (= target typed) "🟢" "🔴")))
    (swap! app-state update :pos inc)
    (when (<= (@app-state :words-max) (@app-state :pos))
      (send-score!)
      (reset-app-state!))))

(defn check-key [key]
  (case key
    " " (check-word)
    "Enter" (check-word)
    "Backspace" (swap! app-state update :errors inc)
    nil))

;;🙅💧💦💔❌🦠🥶🥺
(defn error-component []
  (.log js/console "errors" (:errors @app-state))
  [:div.drill (repeat (:errors @app-state) "💔")])

(defn results-component []
  [:div.drill (apply str (@app-state :results))])

(defn ex-page []
  [:div
   [:h2 "Typing: Challenge"]
   [:p
    {:class "red"}
    "指先見ないで、ゆっくり、確実に。単語間のスペースは一個で。"]
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
   [results-component]
   [:p
    [:input {:type  "button"
             :id    "seconds"
             :class "btn btn-success btn-sm"
             :style {:font-family "monospace"}
             :value (:seconds @app-state)
             :on-click #(do (send-score!) (reset-app-state!))}]
    " 🔚クリックしなくても全部打った後にスペースかエンターでボーナス"]
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
  (reset-app-state!)
  (rdom/render [ex-page] (js/document.getElementById "app"))
  (.focus (.getElementById js/document "drill")))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))
