(ns threevee.face.detector
  (:require
   [boot.core :as c]
   [clojure.math.numeric-tower :as math]
   [threevee.detector.core :as det]
   [threevee.image.core :as img]
   [threevee.image.filter :as ftr]
   [threevee.input.core :as inpt])
  (:import
   [org.opencv.core MatOfRect Point Rect Scalar Size]
   [org.opencv.imgcodecs Imgcodecs]
   [org.opencv.imgproc Imgproc]
   [org.opencv.objdetect CascadeClassifier]))

;; FACE DETECTION / INPUT FILES
(defn indexed-face-rects [face-img detector-config]
  (map-indexed
   (fn [i rect] [i rect])
   (det/detect-faces face-img detector-config)))

(defn indexed-detected-images [images-info detector-config]
  (map (fn [{:keys [image] :as info}]
         (merge info
                {:indexed-face-rects
                 (indexed-face-rects image detector-config)}))
       images-info))

(defn indexed-input-images [input-files detector-config]
  (-> input-files
      inpt/indexed-images
      (indexed-detected-images detector-config)))

(defn detection-preprocess-images [images-info]
  (->> images-info
       (map (fn [{:keys [image] :as m}]
              (merge m {:processed-image
                        (ftr/detection-preprocess image)})))))

(defn indexed-preprocessed-input-images [input-files detector-config]
  (-> input-files
      inpt/indexed-images
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

(defn draw-face-rect [img-rect image]
  (let [a (Point. (.-x img-rect) (.-y img-rect))
        b (Point. (+ (.-x img-rect) (.-width img-rect))
                  (+ (.-y img-rect) (.-height img-rect)))]
    (Imgproc/rectangle image a b (Scalar. 0 255 0) 5)))

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
