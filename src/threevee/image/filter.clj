(ns threevee.image.filter
  (:require
   [clojure.math.numeric-tower :as math])
  (:import
   [org.opencv.core Mat Size]
   [org.opencv.imgproc Imgproc]))

(defn ->code [num-channels]
  (case num-channels
    3 Imgproc/COLOR_BGR2GRAY
    4 Imgproc/COLOR_BGRA2GRAY
    nil))

(defn grayscale [image]
  (let [gray (Mat.)
        channels (.channels image)]
    (if-let [code (->code channels)]
      (do
        (Imgproc/cvtColor image gray code)
        gray)
      image)))

(def SHRINK-WIDTH 320)
(def SHRINK-WIDTH-F (float SHRINK-WIDTH))

(defn shrink [image]
  (let [small-image (Mat.)
        cols (.cols image)
        scale (/ cols SHRINK-WIDTH-F)]
    (if (> cols SHRINK-WIDTH)
      (let [scaled-height (math/round (/ (.rows image) scale))
            size (Size. SHRINK-WIDTH scaled-height)]
        (Imgproc/resize image small-image size)
        small-image)
      image)))

(defn equalize-histogram [image]
  (let [equalized (Mat.)]
    (Imgproc/equalizeHist image equalized)
    equalized))

(defn detection-preprocess [image]
  (-> image
      grayscale
      shrink
      equalize-histogram))
