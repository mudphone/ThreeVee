(ns threevee.face.detector
  (:require
   [boot.core :as c]
   [clojure.java.io :as io]
   [threevee.image.core :as img]
   [threevee.input.core :as inpt])
  (:import
   [org.opencv.core MatOfRect Point Scalar Size]
   [org.opencv.imgcodecs Imgcodecs]
   [org.opencv.imgproc Imgproc]
   [org.opencv.objdetect CascadeClassifier]))

(defn draw-face-rect [img-rect image]
  (let [a (Point. (.-x img-rect) (.-y img-rect))
        b (Point. (+ (.-x img-rect) (.-width img-rect))
                  (+ (.-y img-rect) (.-height img-rect)))]
    (Imgproc/rectangle image a b (Scalar. 0 255 0))))

(def HAAR-CASCADE-CLASSIFIER
  (re-pattern
   "CASCADE_CLASSIFIERS/haarcascade_frontalface_alt\\.xml$"))

(defn haar-face-detector [fileset]
  (let [[name path] (-> (c/by-re [HAAR-CASCADE-CLASSIFIER]
                       (c/input-files fileset))
                        first
                        c/tmp-file
                        inpt/file->name)]
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

(defn indexed-face-rects [face-img detector-config]
  (map-indexed
   (fn [i rect] [i rect])
   (detect-faces face-img detector-config)))

(defn indexed-input-files [input-files detector-config]
  (->> input-files
       (map-indexed
        (fn [i tfile]
          (let [[input-img-name path] (inpt/tmpfile->name tfile)
                face-img (img/image-by-path path)
                idx-face-rects (indexed-face-rects face-img detector-config)]
            [i input-img-name face-img idx-face-rects])))))

(defn- output-path [root img-idx num-rects name]
  (let [file-tag (format "%04d" img-idx)
        rect-tag (format "%02d" num-rects)]
    (str root
         "/" inpt/OUTPUT-FACE-DETECTIONS-DIR
         "/" file-tag
         "_" rect-tag
         "_" name)))

(defn mark-faces-in-image [outdir-path img-idx input-img-name face-img idx-face-rects]
  (let [num-rects (count idx-face-rects)
            face-found? (< 0 num-rects)]
        (when face-found?
          (let [result-path (output-path outdir-path
                                         (inc img-idx)
                                         num-rects
                                         input-img-name)]
            (doseq [[i face-rect] idx-face-rects]
              (draw-face-rect face-rect face-img))
            (println "result-path: " result-path)
            (println "make-parents: " (io/make-parents result-path))
            (println "file written: " (img/save-to-path face-img result-path))))))

(defn detect-and-draw-faces [input-files outdir-path detector-config]
  (let [idx-input-files (indexed-input-files input-files detector-config)]
    (count
     (pmap (fn [[img-idx input-img-name face-img idx-face-rects]]
             (mark-faces-in-image
              outdir-path
              img-idx input-img-name face-img idx-face-rects))
           idx-input-files))))
