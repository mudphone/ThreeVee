(ns threevee.face.preprocess
  (:require
   [clojure.math.numeric-tower :as math]
   [threevee.detector.core :as det]
   [threevee.image.filter :as ftr]
   [threevee.input.core :as inpt])
  (:import
   [org.opencv.core Rect]
   [org.opencv.objdetect Objdetect]))

(def SEARCH-PARAMS
  {:haarcascade-eye
   {:eye-sx 0.16
    :eye-sy 0.26
    :eye-sw 0.30
    :eye-sh 0.28}
   :haarcascade-mcs-lefteye
   {:eye-sx 0.10
    :eye-sy 0.19
    :eye-sw 0.40
    :eye-sh 0.36}
   :haarcascade-lefteye-2splits
   {:eye-sx 0.12
    :eye-sy 0.17
    :eye-sw 0.37
    :eye-sh 0.36}})

(defn search-regions [type]
  (->
   (case type
     (:haarcascade-eye :haarcascade-eye-tree-eyeglasses)
     :haarcascade-eye
     (:haarcascade-mcs-righteye :haarcascade-mcs-lefteye)
     :haarcascade-mcs-lefteye
     (:haarcascade-righteye-2splits :haarcascade-lefteye-2splits)
     :haarcascade-lefteye-2splits
     type)
   (SEARCH-PARAMS)))

(defn eye-submats [classifier-type image]
  (let [{:keys [eye-sx eye-sy eye-sw eye-sh]} (search-regions classifier-type)
        cols (.cols image)
        rows (.rows image)
        left-x (math/round (* cols eye-sx))
        top-y (math/round (* rows eye-sy))
        width-x (math/round (* cols eye-sw))
        height-y (math/round (* rows eye-sh))
        right-x (math/round (* (- 1.0 eye-sx eye-sw)))
        top-left (Rect. left-x top-y width-x height-y)
        top-right (Rect. right-x top-y width-x height-y)]
    [(.submat image top-left)
     (.submat image top-right)]))

;; EYE DETECTOR
(defn detect-an-eye [detector-configs face-submat]
  (reduce (fn [result det-cfg]
              (if-not (empty? result)
                result
                (-> (det/detect-objects face-submat det-cfg)
                    first)))
            nil detector-configs))

(defn detect-eyes [detector-configs classifier-type image]
  (let [[top-left-of-face top-right-of-face] (eye-submats classifier-type image)]
    [(detect-an-eye detector-configs top-left-of-face)
     (detect-an-eye detector-configs top-right-of-face)]))


(defn preprocess [images-info]
  (->> images-info
       (map (fn [{orig-image :image :as m}]
              (let [{shrunk-img :image :as shrunk} (ftr/resize orig-image)]
                (-> m
                    (merge shrunk)
                    (merge {:orig-image orig-image
                            :image (ftr/detection-preprocess shrunk-img)})))))))
