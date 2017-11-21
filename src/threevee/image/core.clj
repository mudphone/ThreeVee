(ns threevee.image.core
  (:require
   [boot.core :as c]
   [clojure.java.io :as io])
  (:import
   [org.opencv.core Mat]
   [org.opencv.imgcodecs Imgcodecs]))

(defn image-by-path [path]
  (Imgcodecs/imread path))

(defn resize-by-rect [image rect]
  (Mat. image rect))

(defn save-to-path [image path]
  (io/make-parents path)
  (Imgcodecs/imwrite path image))
