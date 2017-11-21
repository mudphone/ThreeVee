(ns threevee.detector.core
  (:require
   [boot.core :as c]
   [threevee.input.core :as inpt])
  (:import
   [org.opencv.core MatOfRect Size]
   [org.opencv.objdetect CascadeClassifier Objdetect]))


;; CLASSIFIER
(defn cascade-classifier
  ([classifier-path]
   (let [cc (CascadeClassifier.)]
     (println "loading:" classifier-path)
     (println "loaded:" (.load cc classifier-path))
     cc))
  ([fileset classifier-src-path]
   (let [[name path] (-> (c/by-path [classifier-src-path]
                                    (c/input-files fileset))
                         first
                         c/tmp-file
                         inpt/file->name)]
     (cascade-classifier path))))


;; DETECTOR
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

(defn detect-objects [image {:keys [detector
                                    search-scale-factor
                                    min-neighbors
                                    detection-flags
                                    min-feature-size
                                    max-feature-size]}]
  (let [detected-objs (MatOfRect.)]
    (.detectMultiScale detector
                       image
                       detected-objs
                       search-scale-factor
                       min-neighbors
                       detection-flags
                       min-feature-size
                       max-feature-size)
    (vec (.toArray detected-objs))))


;; EYE DETECTION
(def CASCADE-CLASSIFIERS-ROOT "CASCADE_CLASSIFIERS")
(def OPENCV-HAAR-DIR (str CASCADE-CLASSIFIERS-ROOT
                          "/OpenCV/haarcascades"))
(def CONTRIB-HAAR-DIR (str CASCADE-CLASSIFIERS-ROOT
                           "/contrib/haarcascades"))

(def HAARCASCADE-FRONTALFACE
  (str OPENCV-HAAR-DIR "/haarcascade_frontalface_default.xml"))
(def HAARCASCADE-FRONTALFACE-ALT
  (str OPENCV-HAAR-DIR "/haarcascade_frontalface_alt.xml"))
(def HAARCASCADE-EYE
  (str OPENCV-HAAR-DIR "/haarcascade_eye.xml"))
(def HAARCASCADE-EYE-TREE-EYEGLASSES
  (str OPENCV-HAAR-DIR "/haarcascade_eye_tree_eyeglasses.xml"))
(def HAARCASCADE-MCS-LEFTEYE
  (str CONTRIB-HAAR-DIR "/haarcascade_mcs_lefteye.xml"))
(def HAARCASCADE-MCS-RIGHTEYE
  (str CONTRIB-HAAR-DIR "/haarcascade_mcs_righteye.xml"))
(def HAARCASCADE-LEFTEYE-2SPLITS
  (str OPENCV-HAAR-DIR "/haarcascade_lefteye_2splits.xml"))
(def HAARCASCADE-RIGHTEYE-2SPLITS
  (str OPENCV-HAAR-DIR "/haarcascade_righteye_2splits.xml"))

(defn classifier-type->path
  ([prefix classifier-type]
   (str prefix (classifier-type->path classifier-type)))
  ([classifier-type]
   (case classifier-type
     :haarcascade-frontal-face HAARCASCADE-FRONTALFACE
     :haarcascade-frontal-face-alt HAARCASCADE-FRONTALFACE-ALT
     :haarcascade-eye HAARCASCADE-EYE
     :haarcascade-eye-tree-eyeglasses HAARCASCADE-EYE-TREE-EYEGLASSES
     :haarcascade-mcs-lefteye HAARCASCADE-MCS-LEFTEYE
     :haarcascade-mcs-righteye HAARCASCADE-MCS-RIGHTEYE
     :haarcascade-lefteye-2splits HAARCASCADE-LEFTEYE-2SPLITS
     :haarcascade-righteye-2splits HAARCASCADE-RIGHTEYE-2SPLITS)))

(defn haar-eye-detector
  "Decode type to path, retrieve classifier xml from fileset and 
   return a new classfier"
  ([type]
   (let [get-path #(classifier-type->path "src/" %)]
     (->> type
          get-path
          cascade-classifier)))
  ([type fileset]
   (->> type
        classifier-type->path
        (cascade-classifier fileset))))

(defn haar-eye-detector-config [detector]
  (detector-config
   detector
   {:min-neighbors 4
    :detection-flags Objdetect/CASCADE_FIND_BIGGEST_OBJECT
    :min-feature-size (Size. 20 20)}))


;; FACE DETECTION
(defn haar-face-detector [fileset]
  (let [path (classifier-type->path :haarcascade-frontal-face-alt)]
    (cascade-classifier fileset path)))

;; this is a one-liner, and may be replaced later
(defn detect-faces [image detector-config]
  (detect-objects image detector-config))
