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

(def ^:private version "1.2.0-SNAPSHOT")

(defonce app-state (atom {:text "wait a little"
                          :answer ""
                          :counter 60
                          :errors 0}))
(defonce how-many-typing (atom 0))
(defonce first-key (atom false))

;; こういうのにはコメントしとかないと。
;; report-alert 回数練習したら一度、アラートを出す。
;; この場所で定義するのがいいのか？
(def ^:private report-alert 10)

(defn reset-app-state! []
  (go (let [response (<! (http/get (str "/drill/" 0)))]
        (swap! app-state assoc :text (:body response))))
  (swap! app-state assoc :answer "" :counter 60 :errors 0)
  (reset! first-key false))

;; 0.5.3-SNAPSHOT
;; + counter if finished.
(defn pt [{:keys [text answer counter errors]}]
  (let [s1 (str/split text #"\s+")
        s2 (str/split answer #"\s+")
        s1<>s2 (map list s1 s2)
        all (count s1)
        goods (count (filter (fn [[x y]] (= x y)) s1<>s2))
        bads  (count (remove (fn [[x y]] (= x y)) s1<>s2))
        err   (* -1 (* errors errors))
        score (int (* (- (/ goods all) (/ bads goods)) 100))]
    (js/console.log goods bads all err score)
    (if (= all (+ goods bads))
      (+ score err counter)
      (+ score err))))

(defn nick-pt-message [{:keys [pt users_nick]}]
  (let [s1 (str users_nick " さんのスコアは " pt " 点です。")
        s2 (condp < pt
             100 "すばらしい。最高点取れた？平均で 80 点越えよう。"
              90 "がんばった。もう少しで 100 点だね。"
              60 "だいぶ上手です。この調子でがんばれ。"
              30 "指先を見ずに、ゆっくり、ミスを少なく。"
              "練習あるのみ。")]
    (str s1 "\n" s2)))

;; FIXME: CSRF
;; 本来は post だが、CSRF 問題がクリアできず、get で実装している。
;; request ヘッダ中に見つかる
;; ring.middleware.anti-forgery/anti-forgery-token を利用できないか？
(defn send-score []
  (if (zero? (count (:answer @app-state)))
    (do
      (js/alert "チャレンジ、忘れてるよ。打つべし！")
      (reset-app-state!))
    ;; (go (let))で書いた部分は並列性を持つ、これが非同期ってことか、
    ;; 周りと同じように書いたら混乱する。
    ;; ロジック、実行の順番に注意してプログラムすること。
    (do
      (go (let [response (<! (http/get (str "/score?pt=" (pt @app-state))))]
            (js/alert (nick-pt-message (read-string (:body response))))
            (reset-app-state!)))
      ;; report-alert 回数練習したら一度、アラートを出す。
      ;; この場所で定義するのがいいのか？
      (swap! how-many-typing inc)
      (when (= 0 (mod @how-many-typing report-alert))
        (js/alert "がんばってんねー。一旦、休憩入れたら？")))))

(defn count-down []
  (when @first-key
    (swap! app-state update :counter dec)
    (when (neg? (:counter @app-state))
      (swap! app-state update :counter constantly 0)
      (send-score))))

;;(js/setInterval count-down 1000)

(defn by-dots [n]
  (take n (repeat "🥶"))) ;;🙅💧💦💔❌🦠🥶🥺

(defn check-key [key]
  (if (= key "Backspace")
    (swap! app-state update :errors inc)
    (when-not @first-key
      (swap! first-key not))))

(defn error-component []
  [:p (by-dots (:errors @app-state))])

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
   [:div
    [:input {:type  "button"
             :id    "counter"
             :class "btn btn-primary btn-sm"
             :value (:counter @app-state)
             :on-click send-score}] " 🔚全部打ち終わってクリックするとボーナス"]
  ;;  [:ul
  ;;   [:li [:a {:href "/nickname"} "nickname"]
  ;;    "（変更すると過去データが消える）"]
  ;;   [:li [:a {:href "/password"} "password"]
  ;;    "（忘れるとログインできない）"]]
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
;;   (js/alert "今、タイピングやってるのはレポート終わった人だね？
;; レポートがんばれたか？
;; 困っている人を QA 他で助けるんだぞ。")
;;   (js/alert "レポートやってるか？アップロード試したか？動作チェックしたか？
;; 全てわかっている人以外、
;; レポートで自分が理解してない部分を洗い出した方がいい。
;; 本来レポートはそういうもの。
;; ダメなのはわかったフリをする。")
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop")
  (js/setInterval count-down 9999999))
