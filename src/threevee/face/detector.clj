(ns threevee.face.detector
  (:require
   [boot.core :as c]
   [clojure.java.io :as io]
   [clojure.math.numeric-tower :as math]
   [threevee.image.core :as img]
   [threevee.image.filter :as ftr]
   [threevee.input.core :as inpt])
  (:import
   [org.opencv.core MatOfRect Point Rect Scalar Size]
   [org.opencv.imgcodecs Imgcodecs]
   [org.opencv.imgproc Imgproc]
   [org.opencv.objdetect CascadeClassifier]))

(defn draw-face-rect [img-rect image]
  (let [a (Point. (.-x img-rect) (.-y img-rect))
        b (Point. (+ (.-x img-rect) (.-width img-rect))
                  (+ (.-y img-rect) (.-height img-rect)))]
    (Imgproc/rectangle image a b (Scalar. 0 255 0) 5)))

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
          min-neighbors 7
          detection-flags 0
          min-feature-size (Size. 100 100) ;(Size. 224 224)
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

(defn indexed-images [files]
  (map-indexed (fn [i file]
                 (let [[img-name img-path] (inpt/file->name file)]
                   {:image-index i
                    :image-name img-name
                    :image (img/image-by-path img-path)}))
               files))

(defn indexed-detected-images [images-info detector-config]
  (map (fn [{:keys [image] :as info}]
         (merge info
                {:indexed-face-rects
                 (indexed-face-rects image detector-config)}))
       images-info))

(defn sorted-input-files [input-files]
  (sort-by #(.toString %) input-files))

(defn indexed-input-images [input-files detector-config]
  (-> input-files
      sorted-input-files
      indexed-images
      (indexed-detected-images detector-config)))

(defn detection-preprocess-images [images-info]
  (->> images-info
       (map (fn [{:keys [image] :as m}]
              (merge m {:processed-image
                        (ftr/detection-preprocess image)})))))

(defn indexed-preprocessed-input-images [input-files detector-config]
  (-> input-files
      sorted-input-files
      indexed-images
      detection-preprocess-images
      (indexed-detected-images detector-config)))

(defn- output-path [root out-dir img-idx num-rects name]
  (let [file-tag (format "%04d" img-idx)
        rect-tag (format "%02d" num-rects)]
    (str root
         "/" out-dir
         "/" file-tag
         "_" rect-tag
         "__" name)))

(defn mark-faces-in-image [tmp-path out-dir img-idx input-img-name face-img idx-face-rects]
  (let [num-rects (count idx-face-rects)
            face-found? (< 0 num-rects)]
        (if face-found?
          (let [result-path (output-path tmp-path
                                         out-dir
                                         (inc img-idx)
                                         num-rects
                                         input-img-name)]
            (doseq [[i face-rect] idx-face-rects]
              (draw-face-rect face-rect face-img))
            (println "result-path: " result-path)
            (io/make-parents result-path)
            (println "file written: " (img/save-to-path face-img result-path)))
          (println "no face found: " (inc img-idx) " file: " input-img-name))))

(defn scale-rect [scale rect]
  (if (= scale 1.0)
    rect
    (Rect. (math/round (* scale (.-x rect)))
           (math/round (* scale (.-y rect)))
           (math/round (* scale (.-width rect)))
           (math/round (* scale (.-height rect))))))

(defn detect-and-draw-faces [input-files tmp-path out-dir detector-config]
  (let [files (map c/tmp-file input-files)
        idx-input-files (indexed-preprocessed-input-images files detector-config)]
    (count
     (pmap (fn [{:keys [image-index image-name processed-image indexed-face-rects]}]
             (mark-faces-in-image
              tmp-path
              out-dir
              image-index image-name processed-image indexed-face-rects))
           idx-input-files))))
