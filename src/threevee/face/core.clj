(ns threevee.face.core
  (:require
   [threevee.input.core :as input]))

(defn draw-face-rect [img-rect image]
  (let [a (org.opencv.core.Point. (.-x img-rect) (.-y img-rect))
        b (org.opencv.core.Point. (+ (.-x img-rect) (.-width img-rect))
                                  (+ (.-y img-rect) (.-height img-rect)))]
    (org.opencv.imgproc.Imgproc/rectangle image a b (org.opencv.core.Scalar. 0 255 0))))

(defn detect-face
  ([pic-name]
   (let [face-detector (org.opencv.objdetect.CascadeClassifier. "CASCADE_CLASSIFIERS/haarcascade_frontalface_alt.xml")]
     (detect-face pic-name face-detector)))
  ([[pic-name full-path] face-detector]
   (println "Detecting faces... " pic-name)
   (let [image (org.opencv.imgcodecs.Imgcodecs/imread full-path)
         face-detections (org.opencv.core.MatOfRect.)
         _ (.detectMultiScale face-detector image face-detections)
         img-rects (vec (.toArray face-detections))
         face-count (count img-rects)
         faces-found? (< 0 face-count)]
     (when faces-found?
       (let [output-name (input/output-pic-name pic-name face-count)]
         (println "... output name: " output-name)
         (doseq [img-rect img-rects]
           (draw-face-rect img-rect image))
         (org.opencv.imgcodecs.Imgcodecs/imwrite output-name image))))))

(defn detect-faces []
  (let [face-detector (org.opencv.objdetect.CascadeClassifier. "CASCADE_CLASSIFIERS/haarcascade_frontalface_alt.xml")]
    (doseq [name-and-path (input/input-pic-names)]
      (detect-face name-and-path face-detector))))

(defn p-detect-faces []
  (count
   (pmap #(detect-face %) (input/input-pic-names))))
