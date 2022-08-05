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
   [typing-ex.plot :refer [bar-chart]]))

(def ^:private version "1.13.0")
(def ^:private timeout 60)

(def ^:private todays-limit 10)

(defonce ^:private app-state
  (r/atom  {:text "App is starting..."
            :answer ""
            :seconds timeout
            :errors 0
            :words ""
            :words-max 0
            :pos 0
            :results []
            :todays {}
            :todays-trials 0}))

;; midterm exam
(def mt
  ["An aviator whose plane is forced down in the Sahara Desert
encounters a little prince from a small planet who relates
his adventures in seeking the secret of what is important
in life."
   "Once when I was six years old I saw a beautiful picture in
a book about the primeval forest called 'True Stories'.
It showed a boa constrictor swallowing an animal.
Here is a copy of the drawing."
   "I showed my masterpiece to the grown-ups and asked them if
my drawing frightened them. They answered: 'Why should
anyone be frightened by a hat?' My drawing did not represent
a hat. It was supposed to be a boa constrictor digesting elephant.
"])
(defonce mt-counter (atom 0))

(defn get-login []
  (-> (.getElementById js/document "login")
      (.-value)))

;;; 1.12.x
(def points-debug (atom {}))
;;; pt must not be nagative.
(defn pt-raw [{:keys [text answer seconds errors]}]
  (let [s1 (str/split text #"\s+")
        s2 (str/split answer #"\s+")
        s1<>s2 (map list s1 s2)
        all (count s1)
        goods (count (filter (fn [[x y]] (= x y)) s1<>s2))
        bads  (count (remove (fn [[x y]] (= x y)) s1<>s2))
        err   (* errors errors)
        score (int (* 100 (- (/ goods all) (/ bads goods))))]
    ;; 1.12.x
    (swap! points-debug
           assoc
           :all all :goods goods :bads bads :bs err :bonus seconds)
    (cond
      (< goods 10) 0
      (= all (+ goods bads)) (+ score seconds (- err))
      :else (+ score (- err)))
    #_(if (= all (+ goods bads))
        (+ score (* -1 err) seconds)
        (+ score (* -1 err)))))

(defn pt [args]
  (max 0 (pt-raw args)))

(defn show-score [pt]
  (let [login (get-login)
        s1 (str login " さんのスコアは " pt " 点です。")
        s2 (condp <= pt
             100 "すばらしい。最高点取れた？平均で 80 点越えよう。"
             90 "がんばった。もう少しで 100 点だね。"
             60 "だいぶ上手です。この調子でがんばれ。"
             30 "指先を見ずに、ゆっくり、ミスを少なく。"
             "練習あるのみ。")]
    (when-not (js/confirm (str  s1 "\n" s2 "\n(Cancel でタイプのデータを表示)"))
      (js/alert (str (:text  @app-state)
                     "\n\n"
                     (:answer @app-state)
                     "\n\n"
                     (apply str (:results @app-state))
                     "\n\n"
                     (str @points-debug) "=>" pt)))
    (swap! app-state update :todays-trials inc)
    (when (< todays-limit (:todays-trials @app-state))
      (js/alert "python チュートリアルやってるか？"))));;🐥☕️

(defn csrf-token []
  (.-value (.getElementById js/document "__anti-forgery-token")))

(defn send-score! [pt]
  (http/post "/score"
             {:form-params
              {:pt pt
               :__anti-forgery-token (csrf-token)}}))

;; (go (<!)) は非同期に実行される。
;; 同期プロブラムと同じ気持ちで呑気にプログラムしただけだと、
;; app-state がアップデートされた後のレンダリングが保証されない。
(defn send-fetch-reset! []
  (let [pt (pt @app-state)]
    (go (let [_ (if (zero? (count (:answer @app-state)))
                  (when-not (empty? (:words @app-state))
                    (js/alert "タイプ、忘れた？"))
                  (do
                    (show-score pt)
                    (<! (send-score! pt))))
              {body :body} (<! (http/get (str "/todays/" (get-login))))
              scores (read-string body)
              ;;; midterm exam
              ;;; go の内側で go はいけない。
              {ex? :body} (<! (http/get "/mt"))
              {drill :body}  (if (:b (read-string ex?))
                               (do
                                 (.log js/console "ex mode")
                                 ;;(.log js/console (:b (read-string ex?)))
                                 (swap! mt-counter inc)
                                 {:body (get mt (mod @mt-counter 3))})
                               (do
                                 (.log js/console "normal mode")
                                 (<! (http/get (str "/drill")))))
              words (str/split drill #"\s+")]
          (swap! app-state
                 assoc
                 :text drill
                 :answer ""
                 :seconds timeout
                 :errors 0
                 :words words
                 :words-max (count words)
                 :pos 0
                 :results []
                 :todays scores)
          (.focus (.getElementById js/document "drill"))))))

(defn countdown []
  (swap! app-state update :seconds dec)
  (when (zero? (:seconds @app-state))
    (send-fetch-reset!)))

;; FIXME: when moving below block to top of this code,
;;        becomes not counting down even if declared.
;;(declare countdown)
(defonce updater (js/setInterval countdown 1000))

(defn check-word []
  (let [target (get (@app-state :words) (@app-state :pos))
        typed  (last (str/split (@app-state :answer) #"\s+"))]
    ;;(.log js/console target typed)
    (swap! app-state update :results
           #(conj % (if (= target typed) "🟢" "🔴")))
    (swap! app-state update :pos inc)
    (when (<= (@app-state :words-max) (@app-state :pos))
      (send-fetch-reset!))))

(defn check-key [key]
  (case key
    " " (check-word)
    "Enter" (check-word)
    "Backspace" (swap! app-state update :errors inc)
    nil))

(defn error-component []
  ;;(.log js/console "errors" (:errors @app-state))
  [:div.drill (repeat (:errors @app-state) "🟡")]) ;;🙅💧💦💔❌🦠🥶🥺

(defn results-component []
  [:div.drill (apply str (@app-state :results))])

(defn ex-page []
  ;;(timbre/info "drill" (subs (:drill @app-state) 0 20))
  ;;(timbre/info "todays" (:todays @app-state))
  [:div
   [:h2 "Typing: Challenge"]
   [:p {:class "red"} "指先見ないで、ゆっくり、確実に。単語間のスペースは一個で。"]
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
             :on-click #(do (send-fetch-reset!))}]
    " 🔚 全部打った後にスペースかエンターでボーナス"]
   [:p
    "todays:"
    [:br]
    [bar-chart 300 150 (:todays @app-state)]]
   [:p
    [:a {:href "/sum/1" :class "btn btn-primary btn-sm"} "D.P."]
    " "
    [:a {:href "/logout" :class "btn btn-warning btn-sm"} "logout"]]
   [:hr]
   [:div "hkimura, " version]])

(defn start []
  (send-fetch-reset!)
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
