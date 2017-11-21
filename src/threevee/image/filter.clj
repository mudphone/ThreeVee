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

(def STD-WIDTH 320)
(def STD-WIDTH-F (float STD-WIDTH))

(defn resize
  ([image]
   (resize STD-WIDTH image))
  ([w image]
   (let [new-image (Mat.)
         cols (.cols image)
         scale (/ cols (float w))]
     (if (= cols w)
       {:image image :scale 1.0}
       (let [scaled-height (math/round (/ (.rows image) scale))
             size (Size. w scaled-height)]
         (Imgproc/resize image new-image size)
         {:image new-image :scale scale})))))

(defn shrink [image]
  (if (> (.cols image) STD-WIDTH)
    (resize STD-WIDTH image)
    {:image image :scale 1.0}))

(defn equalize-histogram
  [image]
  (let [equalized (Mat.)]
    (Imgproc/equalizeHist image equalized)
    equalized))

(defn detection-preprocess [image]
  (-> image
      grayscale
      equalize-histogram))
