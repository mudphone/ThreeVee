(ns threevee.face.detector
  (:require
   [boot.core :as c :refer [deftask]]
   [threevee.input.core :as input])
  (:import
   [org.opencv.core MatOfRect Size]
   [org.opencv.imgcodecs Imgcodecs]
   [org.opencv.imgproc Imgproc]
   [org.opencv.objdetect CascadeClassifier]))

(defn draw-face-rect [img-rect image]
  (let [a (org.opencv.core.Point. (.-x img-rect) (.-y img-rect))
        b (org.opencv.core.Point. (+ (.-x img-rect) (.-width img-rect))
                                  (+ (.-y img-rect) (.-height img-rect)))]
    (Imgproc/rectangle image a b (org.opencv.core.Scalar. 0 255 0))))

(def HAAR-CASCADE-CLASSIFIER
  (re-pattern
   "CASCADE_CLASSIFIERS/haarcascade_frontalface_alt\\.xml$"))

(defn haar-face-detector [fileset]
  (let [[name path] (-> (c/by-re [HAAR-CASCADE-CLASSIFIER]
                       (c/input-files fileset))
                        first
                        c/tmp-file
                        input/file->name)]
    (CascadeClassifier. path)))

(defn detector-config
  ([detector]
   (detector-config detector {}))
  ([detector
    {:keys [search-scale-factor
            min-neighbors
            detection-flags
            min-feature-size
            max-feature-size]
     :or {search-scale-factor 1.1
          min-neighbors 4
          detection-flags 0
          min-feature-size (Size. 50 50)
          max-feature-size (Size.)}}]
   {:detector detector
    :search-scale-factor search-scale-factor
    :min-neighbors min-neighbors
    :detection-flags detection-flags
    :min-feature-size min-feature-size
    :max-feature-size max-feature-size}))

(defn detect-faces [image {:keys [detector
                                  search-scale-factor
                                  min-neighbors
                                  detection-flags
                                  min-feature-size
                                  max-feature-size]}]
  (let [face-detections (MatOfRect.)]
    (.detectMultiScale detector
                       image
                       face-detections
                       search-scale-factor
                       min-neighbors
                       detection-flags
                       min-feature-size
                       max-feature-size)
    (vec (.toArray face-detections))))

(defn detect-and-draw-faces
  ([pic-name]
   (detect-and-draw-faces pic-name))
  ([[pic-name full-path] face-detector]
   (println "Detecting faces... " pic-name)
   (let [image (Imgcodecs/imread full-path)
         img-rects (detect-faces [pic-name full-path] face-detector)
         face-count (count img-rects)
         faces-found? (< 0 face-count)]
     (when faces-found?
       (let [output-name (input/output-pic-name pic-name face-count)]
         (println "... output name: " output-name)
         (doseq [img-rect img-rects]
           (draw-face-rect img-rect image))
         (Imgcodecs/imwrite output-name image))))))

(defn detect-faces-in-images []
  (let [face-detector (CascadeClassifier. "CASCADE_CLASSIFIERS/haarcascade_frontalface_alt.xml")]
    (doseq [name-and-path (input/input-pic-names)]
      (detect-and-draw-faces name-and-path face-detector))))

(defn p-detect-faces-in-images []
  (count
   (pmap #(detect-and-draw-faces %) (input/input-pic-names))))
