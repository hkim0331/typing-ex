(ns typing-ex.typing
  ;; (:require-macros
  ;;  [cljs.core.async.macros :refer [go]])
  (:require
   [cljs-http.client :as http]
   [cljs.core.async :refer [go <!]]
   [clojure.string :as str]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [typing-ex.plot :refer [bar-chart]]))


(def ^:private version "v2.8.888")

(def ^:private timeout 60)
(def ^:private todays-limit 10)

(defonce ^:private app-state
  (r/atom  {:text      "App is starting..."
            :answer    ""
            :seconds   timeout
            :errors    0
            :words     ""
            :words-max 0
            :pos       0
            :results   []
            :todays    []
            :todays-trials 0
            :stat "normal"
            :next ""
            :goods 0
            :bads 0}))

(defn csrf-token []
  (.-value (.getElementById js/document "__anti-forgery-token")))


(def little-prince
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

(def moby-dick
  ["Call me Ishmael. Some years ago—never mind how long precisely
having little or no money in my purse, and nothing particular to
interest me on shore, I thought I would sail about a little
and see the watery part of the world."
   "There now is your insular city of the Manhattoes, belted round
by wharves as Indian isles by coral reefs commerce surrounds it
with her surf. Right and left, the streets take you waterward.
Its extreme downtown is the battery, where that noble mole is washed"
   "But look! here come more crowds, pacing straight for the water,
and seemingly bound for a dive. Strange! Nothing will content them
but the extremest limit of the land; loitering under the shady lee
of yonder warehouses will not suffice."])

(def mt little-prince)

(defonce ^:private mt-counter (atom 0))

(def points-debug (atom {}))

;------------------------------------------
(defn get-login []
  (-> (.getElementById js/document "login")
      (.-value)))

(defn pt [{:keys [seconds errors goods bads]}]
  (let [all (:words-max @app-state)
        bs errors ;; backspace key
        score (int (* 100 (- (/ goods all) (/ bads goods))))]
    (swap! points-debug
           assoc
           :all all :goods goods :bads bads :bs bs :seconds seconds)
    (max 0 (cond
             (< goods 10) 0
             (= all goods) (+ score seconds 10) ;; bonus 10
             (= all (+ goods bads)) (+ score seconds (- bs))
             :else (- score bs)))))

(defn show-score
  [pt]
  (if (empty? (:results @app-state))
    (js/alert (str "コピペはダメよ"))
    (let [;; pt (:pt @app-state)
          login (get-login)
          s1 (str login " さんのスコアは " pt " 点です。")
          s2 (condp <= pt
               100 "すばらしい。最高点取れた？平均で 80 点越えよう。"
               90  "がんばった。もう少しで 100 点だね。"
               60  "だいぶ上手です。この調子でがんばれ。"
               30  "指先を見ずに、ゆっくり、ミスを少なく。"
               "練習あるのみ。")
          msg (str  s1 "\n" s2 "\n(Cancel でタイプデータ表示)")]
      (when-not (js/confirm msg)
        (js/alert (str
                   (str @points-debug) " => " pt
                   "\n\n"
                   (:answer @app-state)
                   "\n\n"
                   (apply str (:results @app-state))
                   "\n\n"
                   (:text  @app-state))))))
  ;; VScode, 2024-04-26
  ;; (when (< (:todays-trials @app-state) 3)
  ;;   (js/alert "VScode?"))
  (go (when-let [{:keys [body]} (<! (http/get "/alert"))]
        (when (re-find #"\S" body)
          (js/alert body))))
  ;;
  (swap! app-state update :todays-trials inc)
  (when (< todays-limit (:todays-trials @app-state))
    (js/alert
     (str "連続 "
          (:todays-trials @app-state)
          " 回、行きました。他の勉強もしろよ🐥"))));;🐥☕️

(defn- send-point-aux [url pt]
  (go (let [ret (<! (http/post
                     url
                     {:form-params
                      {:__anti-forgery-token (csrf-token), :pt pt}}))]
        (.log js/console "send-point-aux" url pt ret))))

(defn send-point
  "send-point 中で (:todays @app-state) を更新する。"
  [pt]
  (if (zero? (count (:answer @app-state)))
    (when-not (empty? (:words @app-state))
      (js/alert "タイプ、忘れた？"))
    (do
      (swap! app-state update :todays conj {:pt pt})
      (send-point-aux "/score" pt)
      (when (= "roll-call" (:stat @app-state))
        (send-point-aux "/rc" pt)))))

(defn reset-display!
  []
  (go (let [stat (-> (<! (http/get "/stat")) :body)
            drill (if (= stat "exam")
                    (do
                      (swap! mt-counter inc)
                      (get mt (mod @mt-counter 3)))
                    (-> (<! (http/get "/drill"))
                        :body))
            words (str/split drill #"\s+")
            next (first words)]
        (swap! app-state assoc
               :stat stat
               :text drill
               :answer ""
               :seconds timeout
               :errors 0
               :words words
               :words-max (count words)
               :pos 0
               :results []
               :next next
               :goods 0
               :bads 0)
        (.focus (.getElementById js/document "drill")))))

(defn show-send-reset-display!
  []
  (let [pt (pt @app-state)]
    (show-score pt)
    (send-point pt)
    (reset-display!)))

(defn- next-word []
  (get (:words @app-state) (:pos @app-state)))

(defn check-word []
  (let [target (get (@app-state :words) (@app-state :pos))
        typed  (last (str/split (@app-state :answer) #"\s"))
        good? (= target typed)]

    (swap! app-state update :results
           #(conj % (if good? "🟢" "🔴")))
    (swap! app-state update (if good? :goods :bads) inc)
    (swap! app-state update :pos inc)
    (swap! app-state update :next next-word)
    (when (<= (:words-max @app-state) (:pos @app-state))
      (show-send-reset-display!))))

(defn countdown
  "最初のキーが打たれるまで待つ"
  []
  (when-not (empty? (:answer @app-state))
    (swap! app-state update :seconds dec)
    (when (zero? (:seconds @app-state))
      (show-send-reset-display!))))

(defn check-key [key]
  (case key
    " " (check-word)
    "Enter" (check-word)
    "Backspace" (do
                  (swap! app-state update :errors inc)
                  (swap! app-state update :results conj "🟡"))
    nil))

(defn results-component []
  [:div.drill (apply str (:results @app-state))])

(defn ex-page
  []
  (fn []
    [:div {:class (:stat @app-state)}
     [:h2 "Typing: Challenge"]
     [:p {:class "red"}
      "ノーミスゴールでボーナス。単語間のスペースは一個で。"]
     [:pre {:id "example"} (:text @app-state)]
     [:textarea {:name "answer"
                 :id "drill"
                 :value (:answer @app-state)
                 :on-key-up #(check-key (.-key %))
                 :on-change (fn [e]
                              (swap! app-state
                                     assoc
                                     :answer
                                     (-> e .-target .-value)))}]
     [results-component]
     [:div {:id "next"} (:next @app-state)]
     [:p
      [:input {:type  "button"
               :id    "seconds"
               :class "btn btn-success btn-sm"
               :style {:font-family "monospace"}
               :value (:seconds @app-state)
               :on-click #(do (show-send-reset-display!))}]
      " 🔚 全部タイプした後にスペースかエンターでボーナス"]
     [:p
      "todays:"
      [:br]
      (bar-chart 300 150 (map :pt (:todays @app-state)))]
     [:p
      [:a {:href "/todays" :class "btn btn-danger btn-sm"} "todays"]
      " "
      [:a {:href "/logout" :class "btn btn-warning btn-sm"} "logout"]]
     [:hr]
     [:div "hkimura, " version]]))


(defn start []
  (js/setInterval countdown 1000)
  (reset-display!)
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
